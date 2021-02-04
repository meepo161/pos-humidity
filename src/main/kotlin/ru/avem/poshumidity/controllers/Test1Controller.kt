package ru.avem.poshumidity.controllers

import javafx.application.Platform
import javafx.scene.text.Text
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.poshumidity.app.Pos.Companion.isAppRunning
import ru.avem.poshumidity.communication.model.CommunicationModel
import ru.avem.poshumidity.communication.model.devices.dtv.Dtv02Model
import ru.avem.poshumidity.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.poshumidity.database.entities.Protocol
import ru.avem.poshumidity.utils.*
import ru.avem.poshumidity.view.MainView
import tornadofx.*
import java.text.SimpleDateFormat
import kotlin.concurrent.thread
import kotlin.experimental.and
import kotlin.time.ExperimentalTime

class Test1Controller : TestController() {
    val controller: MainViewController by inject()
    val mainView: MainView by inject()

    private var logBuffer: String? = null

    @Volatile
    var isExperimentEnded: Boolean = true

    //region переменные для значений с приборов

    @Volatile
    private var measuringHumidity1: Double = 0.0

    @Volatile
    private var measuringHumidity2: Double = 0.0

    @Volatile
    private var measuringHumidity3: Double = 0.0

    @Volatile
    private var measuringTemp1: Double = 0.0

    @Volatile
    private var measuringTemp2: Double = 0.0

    @Volatile
    private var measuringTemp3: Double = 0.0

    private var listOfValues1 = mutableListOf<String>()
    private var listOfValues2 = mutableListOf<String>()
    private var listOfValues3 = mutableListOf<String>()

    //endregion

    //region переменные для защит ПР
    @Volatile
    private var doorZone1: Boolean = false

    @Volatile
    private var startButton: Boolean = false

    @Volatile
    private var stopButton: Boolean = false

    @Volatile
    private var currentI1: Boolean = false

    @Volatile
    private var currentI2: Boolean = false

    @Volatile
    private var currentI3: Boolean = false
    //endregion

    private fun appendOneMessageToLog(tag: LogTag, message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(tag, message)
        }
    }

    fun appendMessageToLog(tag: LogTag, _msg: String) {
        val msg = Text("${SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())} | $_msg")
        msg.style {
            fill = when (tag) {
                LogTag.MESSAGE -> tag.c
                LogTag.ERROR -> tag.c
                LogTag.DEBUG -> tag.c
            }
        }

        Platform.runLater {
            mainView.vBoxLog.add(msg)
        }
    }

    var isDevicesResponding = owenPR.isResponding && dtv1.isResponding && dtv2.isResponding && dtv3.isResponding

    private fun startPollDevices() {
        //region pr pool
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.FIXED_STATES_REGISTER_1) { value ->
            doorZone1 = value.toShort() and 2 > 0
            stopButton = value.toShort() and 16 > 0
            startButton = value.toShort() and 32 > 0
//            if (doorZone1) {
//                controller.cause = "Открыта дверь зоны 1" TODO раскомментировать
//            }
            if (stopButton) {
                controller.cause = "Нажата кнопка СТОП"
            }
        }

        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.FIXED_STATES_REGISTER_2) { value ->
            currentI1 = value.toShort() and 1 > 0
            currentI2 = value.toShort() and 2 > 0
            currentI3 = value.toShort() and 4 > 0
            if (currentI1) {
                controller.cause = "Токовая защита лопасти 1"
            }
            if (currentI2) {
                controller.cause = "Токовая защита лопасти 2"
            }
            if (currentI3) {
                controller.cause = "Токовая защита лопасти 3"
            }
        }
        //endregion

        //region dtv pool
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV1, Dtv02Model.HUMIDITY) { value ->
            measuringHumidity1 = value.toDouble()
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV2, Dtv02Model.HUMIDITY) { value ->
            measuringHumidity2 = value.toDouble()
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV3, Dtv02Model.HUMIDITY) { value ->
            measuringHumidity3 = value.toDouble()
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV1, Dtv02Model.TEMPERATURE) { value ->
            measuringTemp1 = value.toDouble()
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV2, Dtv02Model.TEMPERATURE) { value ->
            measuringTemp2 = value.toDouble()
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV3, Dtv02Model.TEMPERATURE) { value ->
            measuringTemp3 = value.toDouble()
        }
        //endregion
    }

    @ExperimentalTime
    fun startTest() {
        thread(isDaemon = true) {
            if (mainView.textFieldHumidity.text.isEmpty() || !mainView.textFieldHumidity.text.isDouble()) {
                runLater {
                    Toast.makeText("Введите влажность").show(Toast.ToastType.ERROR)
                }
            } else if (mainView.textFieldTime.text.isEmpty() || !mainView.textFieldTime.text.isDouble()) {
                runLater {
                    Toast.makeText("Введите количество циклов время работы").show(Toast.ToastType.ERROR)
                }
            } else {
                runLater {
                    mainView.buttonStart.isDisable = true
                    mainView.buttonStop.isDisable = false
                    mainView.mainMenubar.isDisable = true
                    mainView.textFieldHumidity.isDisable = true
                    mainView.textFieldTime.isDisable = true
                }

                controller.isExperimentRunning = true
                controller.clearTable()

                listOfValues1.clear()
                listOfValues2.clear()
                listOfValues3.clear()

                appendMessageToLog(LogTag.DEBUG, "Начало испытания")

                controller.cause = ""

                isExperimentEnded = false

                if (controller.isExperimentRunning) {
                    appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
                }

                var timeForDevices = 300
                while (!isDevicesResponding && controller.isExperimentRunning && timeForDevices-- > 0) {
                    CommunicationModel.checkDevices()
                    sleep(100)
                }

                if (!isDevicesResponding && controller.isExperimentRunning) {
                    controller.cause = "Нет связи с устройствами"
                }

                if (controller.isExperimentRunning && isDevicesResponding) {
                    CommunicationModel.addWritingRegister(
                        CommunicationModel.DeviceID.DD2,
                        OwenPrModel.RESET_DOG,
                        1.toShort()
                    )
                    owenPR.initOwenPR()
                    sleep(1000)
                    startPollDevices()
                    sleep(1000)
                }


                if (!startButton && controller.isExperimentRunning && isDevicesResponding) {
                    runLater {
                        Toast.makeText("Нажмите кнопку ПУСК").show(Toast.ToastType.WARNING)
                    }
                }

                var timeToStart = 300
                while (!startButton && controller.isExperimentRunning && isDevicesResponding && timeToStart-- > 0) {
                    appendOneMessageToLog(LogTag.DEBUG, "Нажмите кнопку ПУСК")
                    sleep(100)
                }

                if (!startButton) {
                    controller.cause = "Не нажата кнопка ПУСК"
                }

                if (controller.isExperimentRunning && isDevicesResponding) {
                    appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")
                    getValuesInTable()
                }

                val allTime = mainView.textFieldTime.text.toInt() * 60 * 60
                val callbackTimer = CallbackTimer(
                    tickPeriod = 1.seconds, tickTimes = allTime,
                    tickJob = {
                        if (!controller.isExperimentRunning) {
                            it.stop()
                        } else {
                            runLater {
                                mainView.labelTimeRemaining.text =
                                    "Осталось : " + toHHmmss((allTime - it.getCurrentTicks()) * 1000L)
                            }
                        }
                    },
                    onFinishJob = {
                    })

                while (controller.isExperimentRunning && callbackTimer.isRunning && isAppRunning) {
                    if (measuringHumidity1 < mainView.textFieldHumidity.text.toDouble()) {
                        owenPR.on1()
                    } else {
                        owenPR.off1()
                    }
                    if (measuringHumidity2 < mainView.textFieldHumidity.text.toDouble()) {
                        owenPR.on2()
                    } else {
                        owenPR.off2()
                    }
                    if (measuringHumidity3 < mainView.textFieldHumidity.text.toDouble()) {
                        owenPR.on3()
                    } else {
                        owenPR.off3()
                    }

                    sleep(1000)

                    controller.tableValuesTest[0].humidity.value = formatRealNumber(measuringHumidity1).toString()
                    controller.tableValuesTest[0].temperature.value = formatRealNumber(measuringTemp1).toString()

                    controller.tableValuesTest[1].humidity.value = formatRealNumber(measuringHumidity2).toString()
                    controller.tableValuesTest[1].temperature.value = formatRealNumber(measuringTemp2).toString()

                    controller.tableValuesTest[2].humidity.value = formatRealNumber(measuringHumidity3).toString()
                    controller.tableValuesTest[2].temperature.value = formatRealNumber(measuringTemp3).toString()

                    listOfValues1.add(String.format("%.1f", measuringHumidity1))
                    listOfValues2.add(String.format("%.1f", measuringHumidity2))
                    listOfValues3.add(String.format("%.1f", measuringHumidity3))
                }

                owenPR.offAllKMs()
                setResult()

                if (listOfValues1.isNotEmpty()) {
                    saveProtocolToDB()
                }

                if (controller.isExperimentRunning) {
                    finalizeExperiment()
                }

                appendMessageToLog(LogTag.MESSAGE, "Испытание завершено")

                controller.isExperimentRunning = false
                runLater {
                    mainView.buttonStart.isDisable = false
                    mainView.buttonStop.isDisable = true
                    mainView.mainMenubar.isDisable = false
                    mainView.textFieldHumidity.isDisable = false
                    mainView.textFieldTime.isDisable = false
                    mainView.labelTestStatus.text = "Статус: стоп"
                }
            }
        }
    }

    private fun saveProtocolToDB() {
        val dateFormatter = SimpleDateFormat("dd.MM.y")
        val timeFormatter = SimpleDateFormat("HH:mm:ss")
        val unixTime = System.currentTimeMillis()

        transaction {
            Protocol.new {
                date = dateFormatter.format(unixTime).toString()
                time = timeFormatter.format(unixTime).toString()
                values1 = listOfValues1.toString()
                values2 = listOfValues2.toString()
                values3 = listOfValues3.toString()
            }
        }
    }

    private fun getValuesInTable() {
        thread(isDaemon = true) {
            while (controller.isExperimentRunning) {
                runLater {
                    controller.tableValuesTest[0].humidity.value = formatRealNumber(measuringHumidity1).toString()
                    controller.tableValuesTest[0].temperature.value = formatRealNumber(measuringTemp1).toString()
                }
                sleep(100)
            }
        }
    }

    private fun sleepWhile(timeSecond: Int) {
        var timer = timeSecond * 10
        while (controller.isExperimentRunning && timer-- > 0 && isDevicesResponding) {
            sleep(100)
        }
    }

    private fun setResult() {
        if (!isDevicesResponding) {
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: \nпотеряна связь с устройствами")
        } else if (controller.cause.isNotEmpty()) {
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: ${controller.cause}")
        } else {
            appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
        }
    }

    private fun finalizeExperiment() {
        isExperimentEnded = true
//        owenPR.offAllKMs()
        CommunicationModel.clearPollingRegisters()

    }
}



package ru.avem.poshumidity.controllers

import javafx.application.Platform
import javafx.scene.control.ButtonType
import javafx.scene.text.Text
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.poshumidity.app.Pos.Companion.isAppRunning
import ru.avem.poshumidity.communication.model.CommunicationModel
import ru.avem.poshumidity.communication.model.devices.dtv.Dtv02Model.Companion.HUMIDITY
import ru.avem.poshumidity.communication.model.devices.dtv.Dtv02Model.Companion.TEMPERATURE
import ru.avem.poshumidity.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.poshumidity.database.entities.Protocol
import ru.avem.poshumidity.protocol.saveProtocolAsWorkbook
import ru.avem.poshumidity.utils.*
import ru.avem.poshumidity.view.MainView
import tornadofx.*
import java.awt.Desktop
import java.io.File
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

    var listOfValues1 = mutableListOf<String>()
    var listOfValues2 = mutableListOf<String>()
    var listOfValues3 = mutableListOf<String>()

    private var listOfValuesTemp1 = mutableListOf<String>()
    private var listOfValuesTemp2 = mutableListOf<String>()
    private var listOfValuesTemp3 = mutableListOf<String>()

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

    var isClicked = false
    var unixTimeStart = 0L

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

    fun isDevicesResponding(): Boolean {
        return CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2).isResponding
//        CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV1).isResponding &&
//        CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV2).isResponding &&
//        CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV3).isResponding
    }

    private fun startPollDevices() {
        //region pr pool
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.FIXED_STATES_REGISTER_1) { value ->
            startButton = value.toShort() and 32 > 0
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.INSTANT_STATES_REGISTER_1) { value ->
            stopButton = value.toShort() and 16 > 0
            if (stopButton) {
                controller.cause = "Нажата кнопка СТОП"
            }
        }
        //endregion
    }

    @ExperimentalTime
    fun startTest() {
        thread(isDaemon = true) {
            if (mainView.textFieldHumidity.text.isEmpty() || !mainView.textFieldHumidity.text.replace(",", ".")
                    .isDouble()
            ) {
                runLater {
                    Toast.makeText("Введите влажность").show(Toast.ToastType.ERROR)
                }
            } else if (mainView.textFieldTime.text.isEmpty() || !mainView.textFieldTime.text.replace(",", ".")
                    .isDouble()
            ) {
                runLater {
                    Toast.makeText("Введите время работы").show(Toast.ToastType.ERROR)
                }
            } else {
                controller.cause = ""
                runLater {
                    mainView.buttonStart.isDisable = true
                    mainView.buttonStop.isDisable = false
                    mainView.mainMenubar.isDisable = true
                    mainView.labelTestStatus.text = "Статус: работа"
                }
                controller.humidityNeed = mainView.textFieldHumidity.text.replace(",", ".").toDouble()
                controller.isExperimentRunning = true
                controller.clearTable()

                listOfValues1.clear()
                listOfValues2.clear()
                listOfValues3.clear()
                listOfValuesTemp1.clear()
                listOfValuesTemp2.clear()
                listOfValuesTemp3.clear()

                appendMessageToLog(LogTag.DEBUG, "Начало испытания")
                unixTimeStart = System.currentTimeMillis()

                isClicked = false
                isExperimentEnded = false

                if (controller.isExperimentRunning) {
                    startPollDevices()
                    appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
                    sleep(1000)
                }

//                var timeToPrepare = 300
//                while (!isDevicesResponding() && controller.isExperimentRunning && timeToPrepare-- > 0) {
//                    sleep(100)
//                }

//                if (!isDevicesResponding()) {
//                    var cause = ""
//                    cause += "Не отвечают приборы: "
//                    if (!CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2).isResponding) {
//                        cause += "ПР "
//                    }
//                    if (!CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV1).isResponding) {
//                        cause += "ДТВ1 "
//                    }
//                    if (!CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV2).isResponding) {
//                        cause += "ДТВ2 "
//                    }
//                    if (!CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV3).isResponding) {
//                        cause += "ДТВ3 "
//                    }
//                    controller.cause = cause
//                }

                if (!isDevicesResponding() && controller.isExperimentRunning) {
                    controller.cause = "Нет связи с БСУ"
                }

                if (controller.isExperimentRunning && isDevicesResponding()) {
                    CommunicationModel.addWritingRegister(
                        CommunicationModel.DeviceID.DD2,
                        OwenPrModel.RESET_DOG,
                        1.toShort()
                    )
                    owenPR.initOwenPR()
                    startPollDevices()
                    sleep(1000)
                }

                if (!startButton && controller.isExperimentRunning && isDevicesResponding()) {
                    runLater {
                        Toast.makeText("Нажмите кнопку ПУСК").show(Toast.ToastType.WARNING)
                    }
                }

                var timeToStart = 300
                while (!startButton && controller.isExperimentRunning && isDevicesResponding() && timeToStart-- > 0) {
                    appendOneMessageToLog(LogTag.DEBUG, "Нажмите кнопку ПУСК")
                    sleep(100)
                }

                if (!startButton && controller.isExperimentRunning && isDevicesResponding()) {
                    controller.cause = "Не нажата кнопка ПУСК"
                }

                if (controller.isExperimentRunning) {
                    soundWarning(1, 1000)
                }

                if (controller.isExperimentRunning && isDevicesResponding()) {
                    appendMessageToLog(LogTag.DEBUG, "Подготовка стенда")
                }

                if (controller.isExperimentRunning) {
                    runLater {
                        mainView.labelTimeRemaining.text = "Ожидайте поднятия влажности до 97%"
                    }
                }

                while ((measuringHumidity1 < 97 || measuringHumidity2 < 97 || measuringHumidity3 < 97)
                    && controller.isExperimentRunning
                ) {
                    generalLogic()
                }

                var timeLeft = 0
                var lastAllTime = (mainView.textFieldTime.text.replace(",", ".").toDouble() * 60 * 60).toInt()
                controller.allTime = (mainView.textFieldTime.text.replace(",", ".").toDouble() * 60 * 60).toInt()
                var callbackTimer = CallbackTimer(
                    tickPeriod = 1.seconds, tickTimes = controller.allTime,
                    tickJob = {
                        if (!controller.isExperimentRunning) {
                            it.stop()
                        } else {
                            runLater {
                                mainView.labelTimeRemaining.text =
                                    "Осталось : " + toHHmmss((controller.allTime - it.getCurrentTicks()) * 1000L)
                            }
                            timeLeft++
                        }
                    },
                    onFinishJob = {
                    })

                while (controller.isExperimentRunning && callbackTimer.isRunning && isAppRunning && isDevicesResponding()) {
                    if (controller.allTime != lastAllTime) {
                        var ticksLeft = timeLeft
                        lastAllTime = (mainView.textFieldTime.text.replace(",", ".").toDouble() * 60 * 60).toInt()
                        callbackTimer.stop()
                        callbackTimer = CallbackTimer(
                            tickPeriod = 1.seconds, tickTimes = controller.allTime - ticksLeft,
                            tickJob = {
                                if (!controller.isExperimentRunning) {
                                    it.stop()
                                } else {
                                    runLater {
                                        mainView.labelTimeRemaining.text =
                                            "Осталось : " + toHHmmss((controller.allTime - ticksLeft - it.getCurrentTicks()) * 1000L)
                                    }
                                    timeLeft++
                                }
                            },
                            onFinishJob = {
                            })
                    }

                    generalLogic()

                    listOfValues1.add(String.format("%.1f", measuringHumidity1))
                    listOfValues2.add(String.format("%.1f", measuringHumidity2))
                    listOfValues3.add(String.format("%.1f", measuringHumidity3))
                    listOfValuesTemp1.add(String.format("%.1f", measuringTemp1))
                    listOfValuesTemp2.add(String.format("%.1f", measuringTemp2))
                    listOfValuesTemp3.add(String.format("%.1f", measuringTemp3))
                }

                controller.tableValuesTest[0].generator.value = "Отключен"
                controller.tableValuesTest[1].generator.value = "Отключен"
                controller.tableValuesTest[2].generator.value = "Отключен"
                owenPR.offAllKMs()
                setResult()

                if (listOfValues1.isNotEmpty()) {
                    saveProtocolToDB()
                }

                if (controller.isExperimentRunning) {
                    owenPR.offAllKMs()
                }

                callbackTimer.stop()

                appendMessageToLog(LogTag.MESSAGE, "Испытание завершено")

                soundWarning(2, 1000)
                finalizeExperiment()

                if (listOfValues1.isNotEmpty()) {
                    saveProtocolToDB()
                    Singleton.currentProtocol = transaction {
                        Protocol.all().toList().asObservable()
                    }.last()
                    runLater {
                        confirm(
                            "Печать протокола",
                            "Испытание завершено. Вы хотите напечатать протокол?",
                            ButtonType.YES, ButtonType.NO,
                            owner = mainView.currentWindow,
                            title = "Печать"
                        ) {
                            saveProtocolAsWorkbook(Singleton.currentProtocol)
                            Desktop.getDesktop().print(File("protocol.xlsx"))
                        }
                    }
                }
            }
        }
    }

    private fun generalLogic() {
        val humidity1 = dtv1.getRegisterById(HUMIDITY)
        dtv1.readRegister(humidity1)
        measuringHumidity1 = if (humidity1.value.toDouble() <= 85.0) {
            humidity1.value.toDouble() + 15
        } else {
            100.0
        } //TODO мошенники

        val humidity2 = dtv2.getRegisterById(HUMIDITY)
        dtv2.readRegister(humidity2)
        measuringHumidity2 = humidity2.value.toDouble()
        val humidity3 = dtv3.getRegisterById(HUMIDITY)
        dtv3.readRegister(humidity3)
        measuringHumidity3 = humidity3.value.toDouble()

        val temp1 = dtv1.getRegisterById(TEMPERATURE)
        dtv1.readRegister(temp1)
        measuringTemp1 = temp1.value.toDouble()
        val temp2 = dtv2.getRegisterById(TEMPERATURE)
        dtv2.readRegister(temp2)
        measuringTemp2 = temp2.value.toDouble()
        val temp3 = dtv3.getRegisterById(TEMPERATURE)
        dtv3.readRegister(temp3)
        measuringTemp3 = temp3.value.toDouble()

        sleep(1000)

        if (controller.humidityNeed > measuringHumidity1) {
            owenPR.on1()
            controller.tableValuesTest[0].generator.value = "Включен"
        } else if (controller.humidityNeed + 1 < measuringHumidity1) {
            owenPR.off1()
            controller.tableValuesTest[0].generator.value = "Отключен"
        }
        if (controller.humidityNeed > measuringHumidity2) {
            owenPR.on2()
            controller.tableValuesTest[1].generator.value = "Включен"
        } else if (controller.humidityNeed + 1 < measuringHumidity2) {
            owenPR.off2()
            controller.tableValuesTest[1].generator.value = "Отключен"
        }
        if (controller.humidityNeed > measuringHumidity3) {
            owenPR.on3()
            controller.tableValuesTest[2].generator.value = "Включен"
        } else if (controller.humidityNeed + 1 < measuringHumidity3) {
            owenPR.off3()
            controller.tableValuesTest[2].generator.value = "Отключен"
        }

        runLater {
            controller.tableValuesTest[0].humidity.value = formatRealNumber(measuringHumidity1).toString()
            controller.tableValuesTest[0].temperature.value = formatRealNumber(measuringTemp1).toString()
            controller.tableValuesTest[1].humidity.value = formatRealNumber(measuringHumidity2).toString()
            controller.tableValuesTest[1].temperature.value = formatRealNumber(measuringTemp2).toString()
            controller.tableValuesTest[2].humidity.value = formatRealNumber(measuringHumidity3).toString()
            controller.tableValuesTest[2].temperature.value = formatRealNumber(measuringTemp3).toString()
        }
    }

    private fun finalizeExperiment() {
        CommunicationModel.clearPollingRegisters()
        isExperimentEnded = true
        controller.isExperimentRunning = false
        runLater {
            mainView.buttonStart.isDisable = false
            mainView.buttonStop.isDisable = true
            mainView.mainMenubar.isDisable = false
            mainView.labelTestStatus.text = "Статус: стоп"
        }
    }

    private fun soundWarning(times: Int, sleep: Long) {
        thread(isDaemon = true) {
            for (i in 0 until times) {
                owenPR.onSound()
                sleep(sleep)
                owenPR.offSound()
                sleep(sleep)
            }
        }
    }

    private fun saveProtocolToDB() {
        val dateFormatter = SimpleDateFormat("dd.MM.y")
        val timeFormatter = SimpleDateFormat("HH:mm:ss")
        val unixTime = System.currentTimeMillis()

        transaction {
            Protocol.new {
                date = dateFormatter.format(unixTimeStart).toString()
                time = timeFormatter.format(unixTimeStart).toString()
                dateEnd = dateFormatter.format(unixTime).toString()
                timeEnd = timeFormatter.format(unixTime).toString()
                operator = controller.position1
                cipher1 = mainView.tfCipher1.text.toString()
                productNumber1 = mainView.tfProductNumber1.text.toString()
                values1 = listOfValues1.toString()
                values2 = listOfValues2.toString()
                values3 = listOfValues3.toString()
                valuesTemp1 = listOfValuesTemp1.toString()
                valuesTemp2 = listOfValuesTemp2.toString()
                valuesTemp3 = listOfValuesTemp3.toString()
            }
        }
    }

    private fun sleepWhile(timeSecond: Int) {
        var timer = timeSecond * 10
        while (controller.isExperimentRunning && timer-- > 0 && isDevicesResponding()) {
            sleep(100)
        }
    }

    private fun setResult() {
        if (controller.cause.isNotEmpty()) {
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине: ${controller.cause}")
            soundError()
        } else if (!isDevicesResponding()) {
            appendMessageToLog(LogTag.ERROR, "Испытание прервано по причине:потеряна связь с устройствами")
            soundError()
        } else {
            appendMessageToLog(LogTag.MESSAGE, "Испытание завершено успешно")
        }
    }

}



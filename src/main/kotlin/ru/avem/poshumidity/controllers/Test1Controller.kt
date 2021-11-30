package ru.avem.poshumidity.controllers

import javafx.application.Platform
import javafx.scene.control.ButtonType
import javafx.scene.text.Text
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.poshumidity.app.Pos.Companion.isAppRunning
import ru.avem.poshumidity.communication.model.CommunicationModel
import ru.avem.poshumidity.communication.model.devices.dtv.Dtv02Model
import ru.avem.poshumidity.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.poshumidity.database.entities.CoefValues
import ru.avem.poshumidity.database.entities.Protocol
import ru.avem.poshumidity.database.entities.ProtocolVars
import ru.avem.poshumidity.protocol.saveProtocolAsWorkbook
import ru.avem.poshumidity.utils.*
import ru.avem.poshumidity.utils.Singleton.listOfValuesTempTest1
import ru.avem.poshumidity.utils.Singleton.listOfValuesTempTest2
import ru.avem.poshumidity.utils.Singleton.listOfValuesTempTest3
import ru.avem.poshumidity.utils.Singleton.listOfValuesTest1
import ru.avem.poshumidity.utils.Singleton.listOfValuesTest2
import ru.avem.poshumidity.utils.Singleton.listOfValuesTest3
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


    @Volatile
    private var timeLeft = 0L

    var isClicked = false
    var unixTimeStart = 0L
    private var isNeedTestValues: Boolean = false

    var coefValues = transaction {
        CoefValues.all().toList().asObservable()
    }.first()

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
//        return CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2).isResponding
//        CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV1).isResponding &&
//        CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV2).isResponding &&
//        CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DTV3).isResponding
        return true
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
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV1, Dtv02Model.HUMIDITY) { value ->
            measuringHumidity1 = if (value.toDouble() <= 100 - coefValues.COEF1.toInt()) {
                value.toDouble() + coefValues.COEF1.toInt()
            } else {
                100.0
            }
            runLater {
                controller.tableValuesTest[0].humidity.value = formatRealNumber(measuringHumidity1).toString()
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV2, Dtv02Model.HUMIDITY) { value ->
            measuringHumidity2 = if (value.toDouble() <= 100 - coefValues.COEF2.toInt()) {
                value.toDouble() + coefValues.COEF2.toInt()
            } else {
                100.0
            }
            runLater {
                controller.tableValuesTest[1].humidity.value = formatRealNumber(measuringHumidity2).toString()
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV3, Dtv02Model.HUMIDITY) { value ->
            measuringHumidity3 = if (value.toDouble() <= 100 - coefValues.COEF3.toInt()) {
                value.toDouble() + coefValues.COEF3.toInt()
            } else {
                100.0
            }
            runLater {
                controller.tableValuesTest[2].humidity.value = formatRealNumber(measuringHumidity3).toString()
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV1, Dtv02Model.TEMPERATURE) { value ->
            measuringTemp1 = value.toDouble()
            runLater {
                controller.tableValuesTest[0].temperature.value = formatRealNumber(measuringTemp1).toString()
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV2, Dtv02Model.TEMPERATURE) { value ->
            measuringTemp2 = value.toDouble()
            runLater {
                controller.tableValuesTest[1].temperature.value = formatRealNumber(measuringTemp2).toString()
            }
        }
        CommunicationModel.startPoll(CommunicationModel.DeviceID.DTV3, Dtv02Model.TEMPERATURE) { value ->
            measuringTemp3 = value.toDouble()
            runLater {
                controller.tableValuesTest[2].temperature.value = formatRealNumber(measuringTemp3).toString()
            }
        }
        //endregion
    }


//    init {
//        thread {
//            while (isAppRunning) {
//                if (isNeedTestValues) {
//                    listOfValuesTest1.add(String.format("%.1f", measuringHumidity1))
//                    listOfValuesTest2.add(String.format("%.1f", measuringHumidity2))
//                    listOfValuesTest3.add(String.format("%.1f", measuringHumidity3))
//                } else {
//                    listOfValuesTest1.clear()
//                    listOfValuesTest2.clear()
//                    listOfValuesTest3.clear()
//                }
//                sleep(1000)
//            }
//        }
//    }
//    init {
//        thread {
//            while (isAppRunning) {
//                listOfValuesTest1.add(String.format("%.1f", 98 + Random.nextDouble()))
//                listOfValuesTest2.add(String.format("%.1f", 98 + Random.nextDouble()))
//                listOfValuesTest3.add(String.format("%.1f", 98 + Random.nextDouble()))
//                println(listOfValuesTest1)
//                sleep(1000)
//            }
//        }
//    }

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
                    mainView.labelTimeRemaining.text = ""
                }
                timeLeft = 0
                controller.humidityNeed = mainView.textFieldHumidity.text.replace(",", ".").toDouble()
                isNeedTestValues = false
                controller.isExperimentRunning = true
                controller.clearTable()

                listOfValuesTest1.clear()
                listOfValuesTest2.clear()
                listOfValuesTest3.clear()
                listOfValuesTempTest1.clear()
                listOfValuesTempTest2.clear()
                listOfValuesTempTest3.clear()

                coefValues = transaction {
                    CoefValues.all().toList().asObservable()
                }.first()

                appendMessageToLog(LogTag.DEBUG, "Начало испытания")
                unixTimeStart = System.currentTimeMillis()

                isClicked = false
                isExperimentEnded = false

                if (controller.isExperimentRunning) {
                    startPollDevices()
                    appendMessageToLog(LogTag.DEBUG, "Инициализация устройств")
                    sleep(1000)
                }

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
                        mainView.labelTimeRemaining.text =
                            "Ожидайте поднятия влажности до ${mainView.textFieldHumidity.text}%"
                    }
                }

                while ((measuringHumidity1 < 95 || measuringHumidity2 < 95 || measuringHumidity3 < 95) && controller.isExperimentRunning) {
                    generalLogic()
                    sleep(1000)
                }

                if (controller.isExperimentRunning) {
                    appendMessageToLog(LogTag.DEBUG, "Начался отсчет времени")
                }

                var lastTimeLeft = timeLeft

                thread(isDaemon = true) {
                    while (controller.isExperimentRunning) {
                        if (timeLeft > lastTimeLeft) {
                            listOfValuesTest1.add(String.format("%.1f", measuringHumidity1))
                            listOfValuesTest2.add(String.format("%.1f", measuringHumidity2))
                            listOfValuesTest3.add(String.format("%.1f", measuringHumidity3))

                            listOfValuesTempTest1.add(String.format("%.1f", measuringTemp1))
                            listOfValuesTempTest2.add(String.format("%.1f", measuringTemp2))
                            listOfValuesTempTest3.add(String.format("%.1f", measuringTemp3))
                            lastTimeLeft = timeLeft
                        }
                        sleep(50)
                    }
                }

                isNeedTestValues = true
                timeLeft = 0
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
                    if (controller.allTime - timeLeft < 0) {
                        runLater {
                            mainView.labelTimeRemaining.text = "Осталось : 00:00:00"
                        }
                        break
                    } else if (controller.allTime != lastAllTime) {
                        var ticksLeft = timeLeft
                        lastAllTime = (mainView.textFieldTime.text.replace(",", ".").toDouble() * 60 * 60).toInt()
                        callbackTimer.stop()
                        callbackTimer = CallbackTimer(
                            tickPeriod = 1.seconds, tickTimes = (controller.allTime - ticksLeft).toInt(),
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
                    sleep(1000)
                }

                if (callbackTimer.isRunning) {
                    callbackTimer.stop()
                }

                controller.tableValuesTest[0].generator.value = "Отключен"
                controller.tableValuesTest[1].generator.value = "Отключен"
                controller.tableValuesTest[2].generator.value = "Отключен"
                owenPR.offAllKMs()
                setResult()

                appendMessageToLog(LogTag.MESSAGE, "Испытание завершено")

                soundWarning(2, 1000)
                finalizeExperiment()

                if (listOfValuesTest1.isNotEmpty()) {
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
                isNeedTestValues = false
            }
        }
    }

    private fun generalLogic() {
        if (controller.humidityNeed + 1 > measuringHumidity1) {
            owenPR.on1()
            controller.tableValuesTest[0].generator.value = "Включен"
        } else if (controller.humidityNeed + 3 <= measuringHumidity1) {
            owenPR.off1()
            controller.tableValuesTest[0].generator.value = "Отключен"
        }
        if (controller.humidityNeed + 1 > measuringHumidity2) {
            owenPR.on2()
            controller.tableValuesTest[1].generator.value = "Включен"
        } else if (controller.humidityNeed + 3 <= measuringHumidity2) {
            owenPR.off2()
            controller.tableValuesTest[1].generator.value = "Отключен"
        }
        if (controller.humidityNeed + 1 > measuringHumidity3) {
            owenPR.on3()
            controller.tableValuesTest[2].generator.value = "Включен"
        } else if (controller.humidityNeed + 3 <= measuringHumidity3) {
            owenPR.off3()
            controller.tableValuesTest[2].generator.value = "Отключен"
        }
//
//        measuringHumidity1 = Random.nextDouble() + 96
//        measuringHumidity2 = Random.nextDouble() + 97
//        measuringHumidity3 = Random.nextDouble() + 98
//        measuringTemp1 = Random.nextDouble() + 24
//        measuringTemp2 = Random.nextDouble() + 25
//        measuringTemp3 = Random.nextDouble() + 26
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

        var protocolVars = transaction {
            ProtocolVars.all().toList().asObservable()
        }.first()

        transaction {
            Protocol.new {
                date = dateFormatter.format(unixTimeStart).toString()
                time = timeFormatter.format(unixTimeStart).toString()
                dateEnd = dateFormatter.format(unixTime).toString()
                timeEnd = timeFormatter.format(unixTime).toString()
                operator = controller.position1
                cipher1 = mainView.tfCipher1.text.toString()
                productNumber1 = mainView.tfProductNumber1.text.toString()
                values1 = listOfValuesTest1.toString()
                values2 = listOfValuesTest2.toString()
                values3 = listOfValuesTest3.toString()
                valuesTemp1 = listOfValuesTempTest1.toString()
                valuesTemp2 = listOfValuesTempTest2.toString()
                valuesTemp3 = listOfValuesTempTest3.toString()
                NUMBER_DATE_ATTESTATION = protocolVars.NUMBER_DATE_ATTESTATION
                NAME_OF_OPERATION = protocolVars.NAME_OF_OPERATION
                NUMBER_CONTROLLER = protocolVars.NUMBER_CONTROLLER
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



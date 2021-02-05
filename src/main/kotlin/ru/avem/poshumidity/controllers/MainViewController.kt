package ru.avem.poshumidity.controllers

import javafx.beans.property.SimpleStringProperty
import ru.avem.poshumidity.app.Pos.Companion.isAppRunning
import ru.avem.poshumidity.communication.model.CommunicationModel
import ru.avem.poshumidity.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.poshumidity.entities.TableValuesTest
import ru.avem.poshumidity.utils.State
import ru.avem.poshumidity.utils.Toast
import ru.avem.poshumidity.view.MainView
import tornadofx.Controller
import tornadofx.asObservable
import tornadofx.runLater
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.experimental.and
import kotlin.time.ExperimentalTime


class MainViewController : Controller() {
    var position1 = ""
    private val view: MainView by inject()

    @Volatile
    var isExperimentRunning: Boolean = false

    var cause: String = ""
        set(value) {
            if (value != "") {
                isExperimentRunning = false
            }
            field = value
        }

    @Volatile
    var humidityNeed: Double = 0.0

    var allTime: Int = 0

    var tableValuesTest = listOf(
        TableValuesTest(
            SimpleStringProperty("Начало"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("Неизвестно")
        ),
        TableValuesTest(
            SimpleStringProperty("Середина"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("Неизвестно")
        ),
        TableValuesTest(
            SimpleStringProperty("Конец"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("Неизвестно")
        )
    ).asObservable()

    init {
        thread(isDaemon = true) {
            runLater {
                view.buttonStop.isDisable = true
            }
            while (isAppRunning) {
                var register = CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2)
                    .getRegisterById(OwenPrModel.INSTANT_STATES_REGISTER_1)
                CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2).readRegister(register)
                var doorZone = register.value.toShort() and 2 > 0

                if (CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2).isResponding) {
                    runLater {
                        view.comIndicate.fill = State.OK.c
                        if (!isExperimentRunning) {
                            view.buttonStart.isDisable = false
                        }
                    }
                } else {
                    runLater {
                        view.comIndicate.fill = State.BAD.c
                        if (!isExperimentRunning) {
                            view.buttonStart.isDisable = true
                        }
                    }
                }
                if (doorZone && CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2).isResponding) {
                    runLater {
                        view.labelTestStatusEnds.text = "Дверь зоны: открыта"
                        if (!isExperimentRunning) {
                            view.buttonStart.isDisable = true
                        }
                    }
                } else if (!doorZone && CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2).isResponding) {
                    runLater {
                        view.labelTestStatusEnds.text = "Дверь зоны: закрыта "
                        if (!isExperimentRunning) {
                            view.buttonStart.isDisable = false
                        }
                    }
                } else {
                    runLater {
                        view.labelTestStatusEnds.text = "Дверь зоны: неизвестно "
                    }
                }
            }
        }
        sleep(1000)
    }

    @OptIn(ExperimentalTime::class)
    fun handleStartTest() {
        Test1Controller().startTest()
    }

    fun handleStopTest() {
        cause = "Отменено оператором"
    }

    fun clearTable() {
        tableValuesTest[0].humidity.value = ""
        tableValuesTest[0].temperature.value = ""
        tableValuesTest[0].generator.value = ""
        tableValuesTest[1].humidity.value = ""
        tableValuesTest[1].temperature.value = ""
        tableValuesTest[1].generator.value = ""
        tableValuesTest[2].humidity.value = ""
        tableValuesTest[2].temperature.value = ""
        tableValuesTest[2].generator.value = ""
        tableValuesTest[0].generator.value = "Отключен"
        tableValuesTest[1].generator.value = "Отключен"
        tableValuesTest[2].generator.value = "Отключен"
    }

    fun showAboutUs() {
        Toast.makeText("Версия ПО: 1.0.0\nВерсия БСУ: 1.0.0\nДата: 02.01.2021").show(Toast.ToastType.INFORMATION)
    }

    fun setNewHumidityAndTime() {
        try {
            humidityNeed = view.textFieldHumidity.text.replace(",", ".").toDouble()
        } catch (e: Exception) {
            Toast.makeText("Проверьте правильность введенных данных влажности").show(Toast.ToastType.ERROR)
            view.textFieldHumidity.clear()
        }
        try {
            allTime = (view.textFieldTime.text.replace(",", ".").toDouble() * 60 * 60).toInt()
        } catch (e: Exception) {
            Toast.makeText("Проверьте правильность введенных данных времени").show(Toast.ToastType.ERROR)
            view.textFieldTime.clear()
        }
    }
}

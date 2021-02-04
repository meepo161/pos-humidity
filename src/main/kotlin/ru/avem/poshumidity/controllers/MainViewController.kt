package ru.avem.poshumidity.controllers

import javafx.beans.property.SimpleStringProperty
import ru.avem.poshumidity.app.Pos.Companion.isAppRunning
import ru.avem.poshumidity.communication.model.CommunicationModel
import ru.avem.poshumidity.entities.TableValuesTest
import ru.avem.poshumidity.utils.State
import ru.avem.poshumidity.utils.Toast
import ru.avem.poshumidity.view.MainView
import tornadofx.Controller
import tornadofx.asObservable
import tornadofx.runLater
import java.lang.Thread.sleep
import kotlin.concurrent.thread
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

    var tableValuesTest = listOf(
        TableValuesTest(
            SimpleStringProperty("Начало"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0")
        ),
        TableValuesTest(
            SimpleStringProperty("Середина"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0")
        ),
        TableValuesTest(
            SimpleStringProperty("Конец"),
            SimpleStringProperty("0.0"),
            SimpleStringProperty("0.0")
        )
    ).asObservable()

    init {
        thread(isDaemon = true) {
            runLater {
                view.buttonStop.isDisable = true
            }
            while (isAppRunning) {
                if (CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2).isResponding) {
                    runLater {
                        view.comIndicate.fill = State.OK.c
                    }
                } else {
                    runLater {
                        view.comIndicate.fill = State.BAD.c
                    }
                }
                sleep(1000)
            }
        }
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
        tableValuesTest[1].humidity.value = ""
        tableValuesTest[1].temperature.value = ""
        tableValuesTest[2].humidity.value = ""
        tableValuesTest[2].temperature.value = ""
    }

    fun showAboutUs() {
        Toast.makeText("Версия ПО: 1.0.0\nВерсия БСУ: 1.0.0\nДата: 02.01.2021").show(Toast.ToastType.INFORMATION)
    }
}

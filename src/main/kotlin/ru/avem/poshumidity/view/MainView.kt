package ru.avem.poshumidity.view

import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.stage.Modality
import ru.avem.poshumidity.controllers.MainViewController
import ru.avem.poshumidity.entities.TableValuesTest
import ru.avem.poshumidity.utils.CallbackTimer
import ru.avem.poshumidity.utils.callKeyBoard
import ru.avem.poshumidity.view.Styles.Companion.extraHard
import ru.avem.poshumidity.view.Styles.Companion.highHard
import ru.avem.poshumidity.view.Styles.Companion.megaHard
import ru.avem.poshumidity.view.Styles.Companion.stopStart
import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess


class MainView : View("Комплексный стенд для испытания ПОС лопасти несущего винта") {
    override val configPath: Path = Paths.get("./app.conf")

    private val controller: MainViewController by inject()

    var mainMenubar: MenuBar by singleAssign()
    var comIndicate: Circle by singleAssign()
    var vBoxLog: VBox by singleAssign()

    var textFieldHumidity: TextField by singleAssign()
    var textFieldTime: TextField by singleAssign()

    var labelTimeRemaining: Label by singleAssign()
    var labelTestStatus: Label by singleAssign()
    var labelTestStatusEnds: Label by singleAssign()
    var buttonApply: Button by singleAssign()

    var buttonStart: Button by singleAssign()
    var buttonStop: Button by singleAssign()

    var tfCipher1: TextField by singleAssign()
    var tfProductNumber1: TextField by singleAssign()

    override fun onDock() {
        super.onDock()
        CallbackTimer(
            tickTimes = 54,
            tickPeriod = 1.hours,
            onStartJob = { println("Started") },
            tickJob = { println("Tick") },
            onFinishJob = { println("Finish") })
    }

    override val root = borderpane {
        maxWidth = 1920.0
        maxHeight = 1000.0
        top {
            mainMenubar = menubar {
                menu("Меню") {
                    item("Сменить пользователя") {
                        action {
                            replaceWith<AuthorizationView>()
                        }
                    }
                    item("Выход") {
                        action {
                            exitProcess(0)
                        }
                    }
                }
                menu("База данных") {
                    item("Переменные для протокола") {
                        action {
                            find<ProtocolVarsWindow>().openModal(
                                modality = Modality.WINDOW_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Протоколы") {
                        action {
                            find<ProtocolListWindow>().openModal(
                                modality = Modality.WINDOW_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Пользователи") {
                        action {
                            find<UserEditorWindow>().openModal(
                                modality = Modality.WINDOW_MODAL,
                                escapeClosesWindow = true,
                                resizable = false,
                                owner = this@MainView.currentWindow
                            )
                        }
                    }
                    item("Администрирование") {
                        action {

                            val dialog: Dialog<*> = TextInputDialog("")
                            dialog.title = "Авторизация"
                            dialog.headerText = "Вход закрыт"
                            dialog.contentText = "Введите пароль: "
                            dialog.showAndWait()
                            if (dialog.result == "4444") {
                                find<AdminWindow>().openModal(
                                    modality = Modality.WINDOW_MODAL,
                                    escapeClosesWindow = true,
                                    resizable = false,
                                    owner = this@MainView.currentWindow
                                )
                            }
                        }
                    }
                }
                menu("Информация") {
                    item("Версия ПО") {
                        action {
                            controller.showAboutUs()
                        }
                    }
                }
            }.addClass(megaHard)
        }
        center {
            anchorpane {
                vbox(spacing = 16.0) {
                    anchorpaneConstraints {
                        leftAnchor = 16.0
                        rightAnchor = 16.0
                        topAnchor = 16.0
                        bottomAnchor = 16.0
                    }
                    alignmentProperty().set(Pos.CENTER)

                    label("Испытание в гидростатической камере").addClass(extraHard)

                    hbox(spacing = 16.0) {
                        alignmentProperty().set(Pos.CENTER)
                        label("Шифр: ")
                        tfCipher1 = textfield {
                            callKeyBoard()
                        }
                        label("Производственный номер: ")
                        tfProductNumber1 = textfield {
                            callKeyBoard()

                        }
                    }.addClass(extraHard)
                    hbox(spacing = 16.0) {
                        alignmentProperty().set(Pos.CENTER)
                        label("Введите влажность: ").addClass(highHard)
                        textFieldHumidity = textfield {
                            prefWidth = 150.0
                            alignment = Pos.CENTER
                            callKeyBoard()
                        }.addClass(highHard)
                        label("%       ").addClass(highHard)
                        label("Время испытания: ").addClass(highHard)
                        textFieldTime = textfield {
                            prefWidth = 150.0
                            alignment = Pos.CENTER
                            callKeyBoard()
                        }.addClass(highHard)
                        label("ч.").addClass(highHard)
                        buttonApply = button("Применить") {
                            action {
                                controller.setNewHumidityAndTime()
                            }
                        }.addClass(highHard)
                    }
                    hbox(spacing = 16.0) {
                        alignmentProperty().set(Pos.CENTER)
                        tableview<TableValuesTest> {
                            items = controller.tableValuesTest
                            minHeight = 376.0
                            maxHeight = 376.0
                            minWidth = 1300.0
                            prefWidth = 1300.0
                            maxWidth = 1300.0
                            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                            mouseTransparentProperty().set(true)
                            column("Место", TableValuesTest::descriptor.getter)
                            column("Влажность, %", TableValuesTest::humidity.getter)
                            column("Температура, °C", TableValuesTest::temperature.getter)
                            column("Парогенератор", TableValuesTest::generator.getter)
                        }.addClass(Styles.mainTable)
                    }
                    hbox {
                        alignmentProperty().set(Pos.CENTER)
                        anchorpane {
                            scrollpane {
                                minHeight = 150.0
                                maxHeight = 150.0
                                prefHeight = 150.0
                                minWidth = 900.0
                                minWidth = 900.0
                                prefWidth = 900.0
                                vBoxLog = vbox {
                                }.addClass(megaHard)
                                vvalueProperty().bind(vBoxLog.heightProperty())
                            }
                        }
                    }

                    hbox(spacing = 16, alignment = Pos.CENTER) {
                        button("Графики влажности") {
                            action {
                                find<GraphicRealTimeWindow>().openModal(
                                    modality = Modality.WINDOW_MODAL,
                                    escapeClosesWindow = true,
                                    resizable = false,
                                    owner = this@MainView.currentWindow
                                )
                            }
                        }
                        button("Графики температуры") {
                            action {
                                find<GraphicTempRealTimeWindow>().openModal(
                                    modality = Modality.WINDOW_MODAL,
                                    escapeClosesWindow = true,
                                    resizable = false,
                                    owner = this@MainView.currentWindow
                                )
                            }
                        }

                    }

                    hbox(spacing = 16) {
                        alignment = Pos.CENTER
                        labelTestStatusEnds = label("")
                        labelTestStatus = label("")
                        labelTimeRemaining = label("")
                    }.addClass(extraHard)
                    hbox(spacing = 16) {
                        alignment = Pos.CENTER
                        buttonStart = button("Запустить") {
                            prefWidth = 640.0
                            prefHeight = 128.0
                            action {
                                controller.handleStartTest()
                            }
                        }.addClass(stopStart)
                        buttonStop = button("Остановить") {
                            prefWidth = 640.0
                            prefHeight = 128.0
                            action {
                                controller.handleStopTest()
                            }
                        }.addClass(stopStart)
                    }
                }
            }
        }
        bottom = hbox(spacing = 32) {
            alignment = Pos.CENTER_LEFT
            comIndicate = circle(radius = 18) {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                    marginLeft = 8.0
                    marginBottom = 8.0
                }
                fill = c("cyan")
                stroke = c("black")
                isSmooth = true
            }
            label(" Связь с ПР") {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                    marginBottom = 8.0
                }
            }
        }
    }.addClass(Styles.blueTheme, megaHard)
}

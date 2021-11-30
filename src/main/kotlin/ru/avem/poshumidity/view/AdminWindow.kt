package ru.avem.poshumidity.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.poshumidity.database.entities.CoefValues
import ru.avem.poshumidity.database.entities.CoefValuesTable
import ru.avem.poshumidity.utils.Toast
import ru.avem.poshumidity.utils.callKeyBoard
import tornadofx.*

class AdminWindow : View("Администрирование") {
    private var textField1: TextField by singleAssign()
    private var textField2: TextField by singleAssign()
    private var textField3: TextField by singleAssign()

    var coefValues = transaction {
        CoefValues.all().toList().asObservable()
    }.first()

    override fun onDock() {
        super.onDock()
        coefValues = transaction {
            CoefValues.all().toList().asObservable()
        }.first()

        textField1.text = coefValues.COEF1
        textField2.text = coefValues.COEF2
        textField3.text = coefValues.COEF3
    }

    override val root = anchorpane {
        vbox(spacing = 16.0) {
            prefWidth = 300.0

            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }

            label("Коэффиценты")
            alignmentProperty().set(Pos.CENTER)
            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("Начало:")
                textField1 = textfield {
                    prefWidth = 200.0
                    callKeyBoard()
                }
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("Середина:")
                textField2 = textfield {
                    prefWidth = 200.0
                    callKeyBoard()
                }
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("Конец")
                textField3 = textfield {
                    prefWidth = 200.0
                    callKeyBoard()
                }
            }
            button("Применить") {
                action {
                    try {
                        transaction {
                            CoefValuesTable.update({
                                CoefValuesTable.COEF1 eq CoefValuesTable.COEF1
                            }) {
                                it[COEF1] = textField1.text
                                it[COEF2] = textField2.text
                                it[COEF3] = textField3.text
                            }
                        }
                        Toast.makeText("Успешно сохранено").show(Toast.ToastType.CONFIRM)
                    } catch (e: Exception) {
                        Toast.makeText("Ошибка").show(Toast.ToastType.ERROR)
                    }
                }
            }
        }
    }.addClass(Styles.medium, Styles.blueTheme)
}

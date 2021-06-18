package ru.avem.poshumidity.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.poshumidity.database.entities.ProtocolVars
import ru.avem.poshumidity.database.entities.ProtocolVarsTable
import ru.avem.poshumidity.utils.Toast
import ru.avem.poshumidity.utils.callKeyBoard
import tornadofx.*

class ProtocolVarsWindow : View("Протоколы графиков") {
    var tfNameOfOperation: TextField by singleAssign()
    var tfNumberAndDateAttestation: TextField by singleAssign()
    var tfNumberController: TextField by singleAssign()
    var protocolVars = transaction {
        ProtocolVars.all().toList().asObservable()
    }.first()

    override fun onDock() {
        super.onDock()
        protocolVars = transaction {
            ProtocolVars.all().toList().asObservable()
        }.first()
        tfNameOfOperation.text = protocolVars.NAME_OF_OPERATION
        tfNumberAndDateAttestation.text = protocolVars.NUMBER_DATE_ATTESTATION
        tfNumberController.text = protocolVars.NUMBER_CONTROLLER

    }

    override val root = anchorpane {
        prefWidth = 1200.0
        prefHeight = 600.0

        vbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }
            alignmentProperty().set(Pos.CENTER)
            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER)
                label("Наименование и шифр технологического процесса:")
                tfNameOfOperation = textfield {
                    minWidth = 400.0
                    callKeyBoard()
                }
            }
            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER)
                label("Номер и дата аттестации:")
                tfNumberAndDateAttestation = textfield {
                    minWidth = 400.0
                    callKeyBoard()
                }
            }
            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER)
                label("Зав.номер контроллера:")
                tfNumberController = textfield {
                    minWidth = 400.0
                    callKeyBoard()
                }
            }
            button("Сохранить") {
                action {
                    try {
                        transaction {
                            ProtocolVarsTable.update({
                                ProtocolVarsTable.NAME_OF_OPERATION eq ProtocolVarsTable.NAME_OF_OPERATION
                            }) {
                                it[NAME_OF_OPERATION] = tfNameOfOperation.text
                                it[NUMBER_DATE_ATTESTATION] = tfNumberAndDateAttestation.text
                                it[NUMBER_CONTROLLER] = tfNumberController.text
                            }
                        }
                        Toast.makeText("Успешно сохранено").show(Toast.ToastType.CONFIRM)
                    } catch (e: Exception) {
                        Toast.makeText("Ошибка").show(Toast.ToastType.ERROR)
                    }
                }
            }
        }
    }.addClass(Styles.blueTheme, Styles.hard)
}
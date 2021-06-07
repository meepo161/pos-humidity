package ru.avem.poshumidity.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.poshumidity.database.entities.User
import ru.avem.poshumidity.utils.callKeyBoard
import ru.avem.poshumidity.view.UserEditorWindow
import tornadofx.*
import tornadofx.controlsfx.warningNotification

class UserAddWindow : View("Добавить пользователя") {
    private val parentView: UserEditorWindow by inject()

    private var textFieldPassword: TextField by singleAssign()
    private var textFieldFullName: TextField by singleAssign()

    override fun onBeforeShow() {
        modalStage!!.setOnHiding {
            parentView.refreshUsersTable()
        }
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

            alignmentProperty().set(Pos.CENTER)
            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("ФИО")
                textFieldFullName = textfield {
                    prefWidth = 200.0

                    callKeyBoard()

                }
            }

            hbox(spacing = 16.0) {
                alignmentProperty().set(Pos.CENTER_RIGHT)

                label("Пароль")
                textFieldPassword = textfield {
                    prefWidth = 200.0

                    callKeyBoard()

                }
            }

            button("Добавить") {
                action {
                    val userPassword = textFieldPassword.text
                    val fullName = textFieldFullName.text

                    if (userPassword.isNullOrEmpty() or fullName.isNullOrEmpty()) {
                        warningNotification(
                            "Заполнение полей",
                            "Заполните все поля и повторите снова.",
                            Pos.BOTTOM_CENTER
                        )
                    } else if (fullName == "admin") {
                        warningNotification(
                            "Заполнение полей",
                            "Нельзя создать admin",
                            Pos.BOTTOM_CENTER
                        )
                    } else {
                        transaction {
                            User.new {
                                this.fullName = fullName
                                password = userPassword
                            }
                        }
                        parentView.refreshUsersTable()
                        textFieldFullName.clear()
                        textFieldPassword.clear()
                        this@UserAddWindow.close()
                    }
                }
            }
        }
    }.addClass(Styles.medium, Styles.blueTheme)
}

package ru.avem.poshumidity.view

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.stage.Modality
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.poshumidity.view.UserAddWindow
import ru.avem.poshumidity.database.entities.User
import ru.avem.poshumidity.database.entities.Users
import ru.avem.poshumidity.database.entities.Users.fullName
import tornadofx.*
import tornadofx.controlsfx.warningNotification

class UserEditorWindow : View("Редактор пользователей") {
    private var tableViewUsers: TableView<User> by singleAssign()

    override fun onBeforeShow() {
        modalStage!!.setOnHiding {
        }
    }

    fun refreshUsersTable() {
        tableViewUsers.items = getUsers()
    }

    private fun getUsers(): ObservableList<User> {
        return transaction {
            User.all().toList().asObservable()
        }
    }

    override val root = anchorpane {
        hbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                bottomAnchor = 16.0
                topAnchor = 16.0
            }

            alignmentProperty().set(Pos.CENTER)

            tableViewUsers = tableview {
                prefWidth = 800.0

                columnResizePolicyProperty().set(TableView.CONSTRAINED_RESIZE_POLICY)

                items = getUsers()

                column("ФИО", User::fullName) {
                    onEditCommit = EventHandler { cell ->
                        transaction {
                            Users.update({
                                fullName eq selectedItem!!.fullName
                            }) {
                                it[fullName] = cell.newValue
                            }
                        }
                    }
                }

//                column("Пароль", User::password) {
//                    onEditCommit = EventHandler { cell ->
//                        transaction {
//                            Users.update({
//                                fullName eq selectedItem!!.fullName
//                            }) {
//                                it[password] = cell.newValue
//                            }
//                        }
//                    }
//                }
            }

            vbox(spacing = 16.0) {
                button("Добавить пользователя") {
                    prefWidth = 300.0

                    action {
                        find<UserAddWindow>().openModal(
                            modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                            owner = this@UserEditorWindow.currentWindow, resizable = false
                        )
                    }
                }

                button("Удалить пользователя") {
                    prefWidth = 300.0

                    action {
                        val item = tableViewUsers.selectedItem
                        if (item != null) {
                            if (item.fullName == "admin") {
                                warningNotification(
                                    "Удаление пользователя",
                                    "Нельзя удалить учетную запись администратора.",
                                    Pos.BOTTOM_CENTER
                                )
                            } else {
                                confirm(
                                    "Удаление пользователя ${item.fullName}",
                                    "Вы действительно хотите удалить пользователя?",
                                    ButtonType.YES, ButtonType.NO,
                                    owner = this@UserEditorWindow.currentWindow,
                                    title = "Удаление пользователя ${item.fullName}"
                                ) {
                                    transaction {
                                        Users.deleteWhere { fullName eq item.fullName }
                                    }
                                    refreshUsersTable()
                                }
                            }
                        }
                    }
                }
            }
        }
    }.addClass(Styles.medium, Styles.blueTheme)
}

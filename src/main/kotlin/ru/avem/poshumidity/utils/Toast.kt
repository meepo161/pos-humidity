package ru.avem.poshumidity.utils

import javafx.geometry.Pos
import org.controlsfx.control.Notifications

class Toast private constructor(private val notifications: Notifications) {
    fun show(type: ToastType?) {
        when (type) {
            ToastType.INFORMATION -> {
                notifications.title("Информация")
                notifications.showInformation()
            }
            ToastType.CONFIRM -> {
                notifications.title("Подтверждение")
                notifications.showConfirm()
            }
            ToastType.ERROR -> {
                notifications.title("Ошибка")
                notifications.showError()
            }
            ToastType.WARNING -> {
                notifications.title("Внимание")
                notifications.showWarning()
            }
            ToastType.NONE -> notifications.show()
            else -> notifications.show()
        }
    }

    enum class ToastType {
        INFORMATION, CONFIRM, ERROR, WARNING, NONE
    }

    companion object {
        fun makeText(text: String?): Toast {
            return Toast(Notifications.create().text(text).position(Pos.CENTER))
        }
    }
}

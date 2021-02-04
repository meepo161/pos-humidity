package ru.avem.poshumidity.app

import javafx.scene.image.Image
import javafx.scene.input.KeyCombination
import javafx.stage.Stage
import ru.avem.poshumidity.database.validateDB
import ru.avem.poshumidity.view.MainView
import ru.avem.poshumidity.view.Styles
import tornadofx.App
import tornadofx.FX

class Pos : App(MainView::class, Styles::class) {

    companion object {
        var isAppRunning = true
    }

    override fun init() {
        validateDB()
    }

    override fun start(stage: Stage) {
        stage.isFullScreen = true
        stage.isResizable = true
//        stage.initStyle(StageStyle.TRANSPARENT)
        stage.fullScreenExitKeyCombination = KeyCombination.NO_MATCH
        super.start(stage)
        FX.primaryStage.icons += Image("icon.png")
    }

    override fun stop() {
        isAppRunning = false
    }
}

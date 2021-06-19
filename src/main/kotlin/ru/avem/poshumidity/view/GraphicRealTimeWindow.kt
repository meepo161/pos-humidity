package ru.avem.poshumidity.view

import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import ru.avem.poshumidity.controllers.Test1Controller
import tornadofx.*

class GraphicRealTimeWindow : View("Добавить пользователя") {
    private val testController: Test1Controller by inject()

    private var lineChart1: LineChart<Number, Number> by singleAssign()
    private var lineChart2: LineChart<Number, Number> by singleAssign()
    private var lineChart3: LineChart<Number, Number> by singleAssign()

    override fun onBeforeShow() {
        super.onBeforeShow()
        createLineCharts()
    }


    override val root = anchorpane {
        vbox(spacing = 16.0) {
            prefWidth = 1800.0

            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }

            alignmentProperty().set(Pos.CENTER)

            var numberAxis = NumberAxis()
            numberAxis.isForceZeroInRange = false
            lineChart1 = linechart("", numberAxis, NumberAxis()) {
                prefWidth = 1800.0
                prefHeight = 300.0
                animated = false
                createSymbols = false
                isLegendVisible = false
                yAxis.label = "Влажность, %"
                xAxis.label = "Время, час"
            }
            separator { }
            numberAxis = NumberAxis()
            numberAxis.isForceZeroInRange = false
            lineChart2 = linechart("", numberAxis, NumberAxis()) {
                prefWidth = 1800.0
                prefHeight = 300.0
                animated = false
                createSymbols = false
                isLegendVisible = false
                yAxis.label = "Влажность, %"
                xAxis.label = "Время, час"
            }
            separator { }
            numberAxis = NumberAxis()
            numberAxis.isForceZeroInRange = false
            lineChart3 = linechart("", numberAxis, NumberAxis()) {
                prefWidth = 1800.0
                prefHeight = 300.0
                animated = false
                createSymbols = false
                isLegendVisible = false
                yAxis.label = "Влажность, %"
                xAxis.label = "Время, час"
            }
        }
    }.addClass(Styles.medium, Styles.blueTheme)

    private fun createLineCharts() {
        val series1 = XYChart.Series<Number, Number>()
        series1.data.clear()
        lineChart1.data.clear()
        lineChart1.title = "Начало ДТВ1"
        val series2 = XYChart.Series<Number, Number>()
        series2.data.clear()
        lineChart2.data.clear()
        lineChart2.title = "Середина ДТВ2"
        val series3 = XYChart.Series<Number, Number>()
        series3.data.clear()
        lineChart3.data.clear()
        lineChart3.title = "Конец ДТВ3"
        var sec = 0

        var step = 1

        if (testController.listOfValues1.size > 10000) {
            step = (testController.listOfValues1.size - testController.listOfValues1.size % 10000) / 10000
        }

        val valuesForTable1 = arrayListOf<Double>()
        val valuesForTable2 = arrayListOf<Double>()
        val valuesForTable3 = arrayListOf<Double>()
        for (i in testController.listOfValues1.indices step step) {
            valuesForTable1.add(testController.listOfValues1[i].replace(',', '.').toDouble())
            valuesForTable2.add(testController.listOfValues2[i].replace(',', '.').toDouble())
            valuesForTable3.add(testController.listOfValues3[i].replace(',', '.').toDouble())
        }

        valuesForTable1.forEach {
            series1.data.add(XYChart.Data(sec * step / 3600, it))
            sec++
        }
        sec = 0
        valuesForTable2.forEach {
            series2.data.add(XYChart.Data(sec * step / 3600, it))
            sec++
        }
        sec = 0
        valuesForTable3.forEach {
            series3.data.add(XYChart.Data(sec * step / 3600, it))
            sec++
        }
        lineChart1.data.add(series1)
        lineChart2.data.add(series2)
        lineChart3.data.add(series3)
    }
}

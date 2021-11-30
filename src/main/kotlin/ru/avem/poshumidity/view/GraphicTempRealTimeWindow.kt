package ru.avem.poshumidity.view

import javafx.geometry.Pos
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.CheckBox
import ru.avem.poshumidity.utils.Singleton.listOfValuesTempTest1
import ru.avem.poshumidity.utils.Singleton.listOfValuesTempTest2
import ru.avem.poshumidity.utils.Singleton.listOfValuesTempTest3
import tornadofx.*

class GraphicTempRealTimeWindow : View("Графики в реальном времени") {

    private var lineChart1: LineChart<Number, Number> by singleAssign()
    private var lineChart2: LineChart<Number, Number> by singleAssign()
    private var lineChart3: LineChart<Number, Number> by singleAssign()
    private var checkbox1: CheckBox by singleAssign()
    private var checkbox2: CheckBox by singleAssign()
    private var checkbox3: CheckBox by singleAssign()

    override fun onBeforeShow() {
        super.onBeforeShow()
        createLineCharts()
        checkbox1.isSelected = true
        checkbox2.isSelected = true
        checkbox3.isSelected = true
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

            checkbox1 = checkbox("Начало") {
                action {
                    if (isSelected) {
                        lineChart1.show()
                    } else {
                        lineChart1.hide()
                    }
                }
            }

            var numberAxis = NumberAxis()
            numberAxis.isForceZeroInRange = false
            lineChart1 = linechart("", numberAxis, NumberAxis()) {
                prefWidth = 1800.0
                prefHeight = 800.0
                animated = false
                createSymbols = false
                isLegendVisible = false
                yAxis.label = "Температура, °C"
                xAxis.label = "Время, сек"
            }
            checkbox2 = checkbox("Середина") {
                action {
                    if (isSelected) {
                        lineChart2.show()
                    } else {
                        lineChart2.hide()
                    }
                }
            }
            numberAxis = NumberAxis()
            numberAxis.isForceZeroInRange = false
            lineChart2 = linechart("", numberAxis, NumberAxis()) {
                prefWidth = 1800.0
                prefHeight = 800.0
                animated = false
                createSymbols = false
                isLegendVisible = false
                yAxis.label = "Температура, °C"
                xAxis.label = "Время, сек"
            }
            checkbox3 = checkbox("Конец") {
                action {
                    if (isSelected) {
                        lineChart3.show()
                    } else {
                        lineChart3.hide()
                    }
                }
            }
            numberAxis = NumberAxis()
            numberAxis.isForceZeroInRange = false
            lineChart3 = linechart("", numberAxis, NumberAxis()) {
                prefWidth = 1800.0
                prefHeight = 800.0
                animated = false
                createSymbols = false
                isLegendVisible = false
                yAxis.label = "Температура, °C"
                xAxis.label = "Время, сек"
            }
            button("Обновить") {
                action {
                    createLineCharts()
                }
            }
        }
    }.addClass(Styles.medium, Styles.blueTheme)

    private fun createLineCharts() {
        val series1 = XYChart.Series<Number, Number>()
        lineChart1.data.clear()
        lineChart1.title = "Начало ДТВ1"
        val series2 = XYChart.Series<Number, Number>()
        lineChart2.data.clear()
        lineChart2.title = "Середина ДТВ2"
        val series3 = XYChart.Series<Number, Number>()
        lineChart3.data.clear()
        lineChart3.title = "Конец ДТВ3"
        var sec = 0

        var step = 1

        if (listOfValuesTempTest1.size > 1000) {
            step = (listOfValuesTempTest1.size - listOfValuesTempTest1.size % 1000) / 1000
        }

        val valuesForTable1 = arrayListOf<Double>()
        val valuesForTable2 = arrayListOf<Double>()
        val valuesForTable3 = arrayListOf<Double>()
        for (i in listOfValuesTempTest1.indices step step) {
            valuesForTable1.add(listOfValuesTempTest1[i].replace(',', '.').toDouble())
            valuesForTable2.add(listOfValuesTempTest2[i].replace(',', '.').toDouble())
            valuesForTable3.add(listOfValuesTempTest3[i].replace(',', '.').toDouble())
        }
        if (listOfValuesTempTest1.size > 1000) {
            valuesForTable1.forEach {
                series1.data.add(XYChart.Data(sec * step, it))
                sec++
            }
            sec = 0
            valuesForTable2.forEach {
                series2.data.add(XYChart.Data(sec * step, it))
                sec++
            }
            sec = 0
            valuesForTable3.forEach {
                series3.data.add(XYChart.Data(sec * step, it))
                sec++
            }
        } else {
            valuesForTable1.forEach {
                series1.data.add(XYChart.Data(sec, it))
                sec++
            }
            sec = 0
            valuesForTable2.forEach {
                series2.data.add(XYChart.Data(sec, it))
                sec++
            }
            sec = 0
            valuesForTable3.forEach {
                series3.data.add(XYChart.Data(sec, it))
                sec++
            }
        }
        lineChart1.data.add(series1)
        lineChart2.data.add(series2)
        lineChart3.data.add(series3)
    }
}

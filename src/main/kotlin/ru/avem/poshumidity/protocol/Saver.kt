package ru.avem.poshumidity.protocol

import javafx.scene.chart.Axis
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.charts.AxisPosition
import org.apache.poi.ss.usermodel.charts.ChartDataSource
import org.apache.poi.ss.usermodel.charts.DataSources
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xddf.usermodel.chart.AxisCrosses
import org.apache.poi.xddf.usermodel.chart.AxisTickMark
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFChart
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean
import org.openxmlformats.schemas.drawingml.x2006.chart.CTTickMark
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickMark
import org.openxmlformats.schemas.drawingml.x2006.main.STPenAlignment
import ru.avem.poshumidity.app.Pos
import ru.avem.poshumidity.database.entities.Protocol
import ru.avem.poshumidity.database.entities.ProtocolSingle
import ru.avem.poshumidity.utils.Singleton.maxIndex1
import ru.avem.poshumidity.utils.Singleton.maxIndex2
import ru.avem.poshumidity.utils.Singleton.maxIndex3
import ru.avem.poshumidity.utils.Singleton.maxIndexTemp1
import ru.avem.poshumidity.utils.Singleton.maxIndexTemp2
import ru.avem.poshumidity.utils.Singleton.maxIndexTemp3
import ru.avem.poshumidity.utils.Singleton.minIndex1
import ru.avem.poshumidity.utils.Singleton.minIndex2
import ru.avem.poshumidity.utils.Singleton.minIndex3
import ru.avem.poshumidity.utils.Singleton.minIndexTemp1
import ru.avem.poshumidity.utils.Singleton.minIndexTemp2
import ru.avem.poshumidity.utils.Singleton.minIndexTemp3
import ru.avem.poshumidity.utils.Toast
import ru.avem.poshumidity.utils.copyFileFromStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.util.*

var TO_DESIRED_ROW = 0

fun saveProtocolAsWorkbook(protocol: Protocol, path: String = "protocol.xlsx") {
    val template = File(path)
    copyFileFromStream(Pos::class.java.getResource("protocol.xlsx").openStream(), template)

    try {
        XSSFWorkbook(template).use { wb ->
            val sheet = wb.getSheetAt(0)
            for (iRow in 0 until 100) {
                val row = sheet.getRow(iRow)
                if (row != null) {
                    for (iCell in 0 until 100) {
                        val cell = row.getCell(iCell)
                        if (cell != null && (cell.cellType == CellType.STRING)) {
                            when (cell.stringCellValue) {
                                "#PROTOCOL_NUMBER#" -> cell.setCellValue(protocol.id.toString())
                                "#DATE#" -> cell.setCellValue(protocol.date)
                                "#TIME#" -> cell.setCellValue(protocol.time)
                                "#DATE_END#" -> cell.setCellValue(protocol.dateEnd)
                                "#TIME_END#" -> cell.setCellValue(protocol.timeEnd)
                                "#CIPHER1#" -> cell.setCellValue(protocol.cipher1)
                                "#NUMBER_PRODUCT1#" -> cell.setCellValue(protocol.productNumber1)
                                "#OPERATOR#" -> cell.setCellValue(protocol.operator)
                                "#NUMBER_DATE_ATTESTATION#" -> cell.setCellValue(protocol.NUMBER_DATE_ATTESTATION)
                                "#NAME_OF_OPERATION#" -> cell.setCellValue(protocol.NAME_OF_OPERATION)
                                "#NUMBER_CONTROLLER#" -> cell.setCellValue(protocol.NUMBER_CONTROLLER)

                                else -> {
                                    if (cell.stringCellValue.contains("#")) {
                                        cell.setCellValue("")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            fillParameters3(
                wb,
                protocol.values1,
                protocol.values2,
                protocol.values3,
                protocol.valuesTemp1,
                protocol.valuesTemp2,
                protocol.valuesTemp3,
                0,
                15
            )
            drawLineChart3(wb)

            val sheet2 = wb.getSheetAt(1)
            for (iRow in 0 until 200) {
                val row = sheet2.getRow(iRow)
                if (row != null) {
                    for (iCell in 0 until 200) {
                        val cell = row.getCell(iCell)
                        if (cell != null && (cell.cellType == CellType.STRING)) {
                            when (cell.stringCellValue) {
                                "#PROTOCOL_NUMBER#" -> cell.setCellValue(protocol.id.toString())
                                "#DATE#" -> cell.setCellValue(protocol.date)
                                "#TIME#" -> cell.setCellValue(protocol.time)
                                "#DATE_END#" -> cell.setCellValue(protocol.dateEnd)
                                "#TIME_END#" -> cell.setCellValue(protocol.timeEnd)
                                "#CIPHER1#" -> cell.setCellValue(protocol.cipher1)
                                "#NUMBER_PRODUCT1#" -> cell.setCellValue(protocol.productNumber1)
                                "#OPERATOR#" -> cell.setCellValue(protocol.operator)
                                "#NUMBER_DATE_ATTESTATION#" -> cell.setCellValue(protocol.NUMBER_DATE_ATTESTATION)
                                "#NAME_OF_OPERATION#" -> cell.setCellValue(protocol.NAME_OF_OPERATION)
                                "#NUMBER_CONTROLLER#" -> cell.setCellValue(protocol.NUMBER_CONTROLLER)

                                else -> {
                                    if (cell.stringCellValue.contains("#")) {
                                        cell.setCellValue("")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            sheet.protectSheet("avem")
            val outStream = ByteArrayOutputStream()
            wb.write(outStream)
            outStream.close()
        }
    } catch (e: FileNotFoundException) {
        Toast.makeText("Не удалось сохранить протокол на диск")
    }
}

fun saveProtocolAsWorkbook(protocolSingle: ProtocolSingle, path: String = "protocol.xlsx", start: Int, end: Int) {
    val template = File(path)
    copyFileFromStream(Pos::class.java.getResource("protocol.xlsx").openStream(), template)

    try {
        XSSFWorkbook(template).use { wb ->
            val sheet = wb.getSheetAt(0)
            for (iRow in 0 until 100) {
                val row = sheet.getRow(iRow)
                if (row != null) {
                    for (iCell in 0 until 100) {
                        val cell = row.getCell(iCell)
                        if (cell != null && (cell.cellType == CellType.STRING)) {
                            when (cell.stringCellValue) {
                                "#PROTOCOL_NUMBER#" -> cell.setCellValue(protocolSingle.id.toString())
                                "#DATE#" -> cell.setCellValue(protocolSingle.date)
                                "#TIME#" -> cell.setCellValue(protocolSingle.time)

                                else -> {
                                    if (cell.stringCellValue.contains("#")) {
                                        cell.setCellValue("")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            fillParameters(wb, protocolSingle.values, start, end)
            drawLineChart(wb)
            sheet.protectSheet("avem")
            val outStream = ByteArrayOutputStream()
            wb.write(outStream)
            outStream.close()
        }
    } catch (e: FileNotFoundException) {
        Toast.makeText("Не удалось сохранить протокол на диск")
    }
}

fun fillParameters(wb: XSSFWorkbook, dots: String, start: Int, end: Int) {
    var values = dots.removePrefix("[").removePrefix("'").removeSuffix("]")
        .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
    val valuesForExcel = arrayListOf<Double>()
    for (i in values.indices) {
        valuesForExcel.add(values[i])
    }
    val sheet = wb.getSheetAt(0)
    var row: Row
    var cellStyle: XSSFCellStyle = generateStyles(wb) as XSSFCellStyle
    var rowNum = sheet.lastRowNum + 1
    row = sheet.createRow(rowNum)
    var columnNum = 0
    for (i in values.indices) {
        columnNum = fillOneCell(row, columnNum, cellStyle, i + start)
        columnNum = fillOneCell(row, columnNum, cellStyle, values[i])
        row = sheet.createRow(++rowNum)
        columnNum = 0
    }
}

fun fillParameters3(
    wb: XSSFWorkbook,
    dots1: String,
    dots2: String,
    dots3: String,
    dotsTemp1: String,
    dotsTemp2: String,
    dotsTemp3: String,
    columnNumber: Int,
    rawNumber: Int
) {
    val values1 = dots1.removePrefix("[").removePrefix("'").removeSuffix("]")
        .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
    val values2 = dots2.removePrefix("[").removePrefix("'").removeSuffix("]")
        .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
    val values3 = dots3.removePrefix("[").removePrefix("'").removeSuffix("]")
        .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
    val valuesTemp1 = dotsTemp1.removePrefix("[").removePrefix("'").removeSuffix("]")
        .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
    val valuesTemp2 = dotsTemp2.removePrefix("[").removePrefix("'").removeSuffix("]")
        .split(", ").map { it.replace(',', '.') }.map(String::toDouble)
    val valuesTemp3 = dotsTemp3.removePrefix("[").removePrefix("'").removeSuffix("]")
        .split(", ").map { it.replace(',', '.') }.map(String::toDouble)

    val valuesForExcel1 = arrayListOf<Double>()
    val valuesForExcel2 = arrayListOf<Double>()
    val valuesForExcel3 = arrayListOf<Double>()
    val valuesForExcelTemp1 = arrayListOf<Double>()
    val valuesForExcelTemp2 = arrayListOf<Double>()
    val valuesForExcelTemp3 = arrayListOf<Double>()
    var step = 1
    if (values1.size > 200) {
        step = (values1.size - values1.size % 200) / 200
    }
    for (i in values1.indices step step) {
        valuesForExcel1.add(values1[i])
        valuesForExcel2.add(values2[i])
        valuesForExcel3.add(values3[i])
        valuesForExcelTemp1.add(valuesTemp1[i])
        valuesForExcelTemp2.add(valuesTemp2[i])
        valuesForExcelTemp3.add(valuesTemp3[i])
    }
    valuesForExcel1.indexOf(Collections.min(valuesForExcel1)).apply {
        minIndex1 = valuesForExcel1[this]
    }
    valuesForExcel2.indexOf(Collections.min(valuesForExcel2)).apply {
        minIndex2 = valuesForExcel2[this]
    }
    valuesForExcel3.indexOf(Collections.min(valuesForExcel3)).apply {
        minIndex3 = valuesForExcel3[this]
    }
    valuesForExcel1.indexOf(Collections.max(valuesForExcel1)).apply {
        maxIndex1 = valuesForExcel1[this]
    }
    valuesForExcel2.indexOf(Collections.max(valuesForExcel2)).apply {
        maxIndex2 = valuesForExcel2[this]
    }
    valuesForExcel3.indexOf(Collections.max(valuesForExcel3)).apply {
        maxIndex3 = valuesForExcel3[this]
    }
    valuesForExcelTemp1.indexOf(Collections.min(valuesForExcelTemp1)).apply {
        minIndexTemp1 = valuesForExcelTemp1[this]
    }
    valuesForExcelTemp2.indexOf(Collections.min(valuesForExcelTemp2)).apply {
        minIndexTemp2 = valuesForExcelTemp2[this]
    }
    valuesForExcelTemp3.indexOf(Collections.min(valuesForExcelTemp3)).apply {
        minIndexTemp3 = valuesForExcelTemp3[this]
    }
    valuesForExcelTemp1.indexOf(Collections.max(valuesForExcelTemp1)).apply {
        maxIndexTemp1 = valuesForExcelTemp1[this]
    }
    valuesForExcelTemp2.indexOf(Collections.max(valuesForExcelTemp2)).apply {
        maxIndexTemp2 = valuesForExcelTemp2[this]
    }
    valuesForExcelTemp3.indexOf(Collections.max(valuesForExcelTemp3)).apply {
        maxIndexTemp3 = valuesForExcelTemp3[this]
    }
    val sheet = wb.getSheetAt(0)
    var row: Row
    val cellStyle: XSSFCellStyle = generateStyles(wb) as XSSFCellStyle
    var rowNum = rawNumber
    row = sheet.createRow(rowNum)
    var second = 0
    for (i in valuesForExcel1.indices) {
        fillOneCell(row, columnNumber, cellStyle, (second / 60.0 / 60).toInt())
        fillOneCell(row, columnNumber + 1, cellStyle, valuesForExcel1[i])
        fillOneCell(row, columnNumber + 2, cellStyle, valuesForExcel2[i])
        fillOneCell(row, columnNumber + 3, cellStyle, valuesForExcel3[i])
        fillOneCell(row, columnNumber + 4, cellStyle, valuesForExcelTemp1[i])
        fillOneCell(row, columnNumber + 5, cellStyle, valuesForExcelTemp2[i])
        fillOneCell(row, columnNumber + 6, cellStyle, valuesForExcelTemp3[i])
        row = sheet.createRow(++rowNum)
        second += step
    }

}

private fun drawLineChart(workbook: XSSFWorkbook) {
    val sheet = workbook.getSheet("Sheet1")
    val lastRowIndex = sheet.lastRowNum - 1
    val timeData = DataSources.fromNumericCellRange(sheet, CellRangeAddress(16, lastRowIndex, 0, 0))
    val valueData = DataSources.fromNumericCellRange(sheet, CellRangeAddress(16, lastRowIndex, 1, 1))

    var lineChart = createLineChart(sheet)
    drawLineChart3(lineChart, timeData, valueData, valueData)
}

private fun createLineChart(sheet: XSSFSheet): XSSFChart {
    val drawing = sheet.createDrawingPatriarch()
    val anchor = drawing.createAnchor(0, 0, 0, 0, 2, 16, 36, 26)

    return drawing.createChart(anchor)
}

private fun fillOneCell(row: Row, columnNum: Int, cellStyle: XSSFCellStyle, points: Double): Int {
    val cell: Cell = row.createCell(columnNum)
    cell.cellStyle = cellStyle
    cell.setCellValue(points)
    return columnNum + 1
}

private fun fillOneCell(row: Row, columnNum: Int, cellStyle: XSSFCellStyle, points: Int): Int {
    val cell: Cell = row.createCell(columnNum)
    cell.cellStyle = cellStyle
    cell.setCellValue(points.toString())
    return columnNum + 1
}

private fun generateStyles(wb: XSSFWorkbook): CellStyle {
    val headStyle: CellStyle = wb.createCellStyle()
    headStyle.wrapText = true
    headStyle.borderBottom = BorderStyle.THIN
    headStyle.borderTop = BorderStyle.THIN
    headStyle.borderLeft = BorderStyle.THIN
    headStyle.borderRight = BorderStyle.THIN
    headStyle.alignment = HorizontalAlignment.CENTER
    headStyle.verticalAlignment = VerticalAlignment.CENTER
    return headStyle
}

private fun drawLineChart3(workbook: XSSFWorkbook) {
    val sheet = workbook.getSheet("Sheet1")
    val sheet2 = workbook.getSheet("Sheet2")
    val lastRowIndex = sheet.lastRowNum - 1

    var i = 0
    val timeData1 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, i, i))
    val valueData1 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, ++i, i))
    val valueData2 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, ++i, i))
    val valueData3 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, ++i, i))
    val valueDataTemp1 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, ++i, i))
    val valueDataTemp2 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, ++i, i))
    val valueDataTemp3 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, ++i, i))

    var lastRowForGraph = 1
    val graphHeight = 41
    val graphSpace = graphHeight + 9
    val lineChart1 = createLineChart(sheet2, lastRowForGraph, lastRowForGraph + graphHeight)
    drawLineChart3(
        lineChart1,
        timeData1,
        valueData1,
        valueDataTemp1,
        "Время, час.      Начало(ДТВ1)",
        "Температура, °C                                                                                               Влажность, %",
        minIndex1,
        maxIndex1
    )
    lastRowForGraph += graphSpace
    val lineChart2 = createLineChart(sheet2, lastRowForGraph, lastRowForGraph + graphHeight)
    drawLineChart3(
        lineChart2,
        timeData1,
        valueData2,
        valueDataTemp2,
        "Время, час.      Середина(ДТВ2)",
        "Температура, °C                                                                                               Влажность, %",
        minIndex2,
        maxIndex2
    )
    lastRowForGraph += graphSpace
    val lineChart3 = createLineChart(sheet2, lastRowForGraph, lastRowForGraph + graphHeight)
    drawLineChart3(
        lineChart3,
        timeData1,
        valueData3,
        valueDataTemp3,
        "Время, час.      Конец(ДТВ3)",
        "Температура, °C                                                                                               Влажность, %",
        minIndex3,
        maxIndex3
    )
    lastRowForGraph += graphSpace
//    val lineChartTemp1 = createLineChart(sheet2, lastRowForGraph, lastRowForGraph + graphHeight)
//    drawLineChart3(
//        lineChartTemp1, timeData1, valueDataTemp1, "Время, час.      Начало(ДТВ1)", "Температура, °C",
//        minIndexTemp1,
//        maxIndexTemp1
//    )
//    lastRowForGraph += graphSpace
//    val lineChartTemp2 = createLineChart(sheet2, lastRowForGraph, lastRowForGraph + graphHeight)
//    drawLineChart3(
//        lineChartTemp2, timeData1, valueDataTemp2, "Время, час.      Середина(ДТВ2)", "Температура, °C",
//        minIndexTemp2,
//        maxIndexTemp2
//    )
//    lastRowForGraph += graphSpace
//    val lineChartTemp3 = createLineChart(sheet2, lastRowForGraph, lastRowForGraph + graphHeight)
//    drawLineChart3(
//        lineChartTemp3, timeData1, valueDataTemp3, "Время, час.      Конец(ДТВ3)", "Температура, °C",
//        minIndexTemp3,
//        maxIndexTemp3
//    )
}

private fun createLineChart(sheet: XSSFSheet, rowStart: Int, rowEnd: Int, col1: Int = 1, col2: Int = 19): XSSFChart {
    val drawing = sheet.createDrawingPatriarch()
    val anchor = drawing.createAnchor(0, 0, 0, 0, col1, rowStart, col2, rowEnd)
    return drawing.createChart(anchor)
}

private fun drawLineChart3(
    lineChart: XSSFChart,
    xAxisData: ChartDataSource<Number>,
    yAxisData: ChartDataSource<Number>,
    yAxisData2: ChartDataSource<Number>,
    section: String = "", title: String = "", min: Double = 0.0, max: Double = 0.0
) {
    val data = lineChart.chartDataFactory.createLineChartData()

    val xAxis = lineChart.chartAxisFactory.createCategoryAxis(AxisPosition.BOTTOM)
    val yAxis = lineChart.createValueAxis(AxisPosition.LEFT)
    yAxis.crosses = org.apache.poi.ss.usermodel.charts.AxisCrosses.AUTO_ZERO

    val series = data.addSeries(xAxisData, yAxisData)
    val series2 = data.addSeries(xAxisData, yAxisData2)
    series.setTitle("График")
    lineChart.plot(data, xAxis, yAxis)
    lineChart.axes[0].setTitle(section)
    lineChart.axes[1].setTitle(title)
    lineChart.axes[1].minimum = 0.0/*(min.toInt() - 2).toDouble()*/
    lineChart.axes[1].maximum = 100.0/*(max.toInt() + 2).toDouble()*/
    lineChart.axes[1].majorUnit = 5.0

    lineChart.axes[0].majorTickMark = AxisTickMark.IN
    lineChart.axes[0].minorTickMark = AxisTickMark.IN

//    val plotArea = lineChart.ctChart.plotArea
//    plotArea.lineChartArray[0].smooth
//    val ctBool = CTBoolean.Factory.newInstance()
//    ctBool.`val` = false
//    plotArea.lineChartArray[0].smooth = ctBool
//    for (series in plotArea.lineChartArray[0].serArray) {
//        series.smooth = ctBool
//    }
//    plotArea.catAxArray[0].addNewMajorGridlines()


//    plotArea.catAxList[0].majorGridlines.spPr
//    plotArea.catAxArray[0].addNewMinorGridlines()

//    plotArea.valAxArray[0].addNewMajorGridlines()
//    plotArea.valAxArray[0].addNewMinorGridlines()
}

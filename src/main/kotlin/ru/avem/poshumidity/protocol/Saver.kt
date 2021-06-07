package ru.avem.poshumidity.protocol

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.usermodel.charts.AxisPosition
import org.apache.poi.ss.usermodel.charts.ChartDataSource
import org.apache.poi.ss.usermodel.charts.DataSources
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFChart
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean
import ru.avem.poshumidity.app.Pos
import ru.avem.poshumidity.database.entities.Protocol
import ru.avem.poshumidity.database.entities.ProtocolSingle
import ru.avem.poshumidity.utils.Toast
import ru.avem.poshumidity.utils.copyFileFromStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException

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
                                "#CIPHER#" -> cell.setCellValue(protocol.cipher1)
                                "#NUMBER_PRODUCT#" -> cell.setCellValue(protocol.productNumber1)
                                "#OPERATOR#" -> cell.setCellValue(protocol.operator)

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
    val sheet = wb.getSheetAt(0)
    var row: Row
    val cellStyle: XSSFCellStyle = generateStyles(wb) as XSSFCellStyle
    var rowNum = rawNumber
    row = sheet.createRow(rowNum)
    var second = 0
    for (i in valuesForExcel1.indices) {
        fillOneCell(row, columnNumber, cellStyle, (second / 60.0).toInt())
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
    drawLineChart3(lineChart, timeData, valueData)
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
    val lastRowIndex = sheet.lastRowNum - 1

    val timeData1 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, 0, 0))
    val valueData1 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, 1, 1))
    val valueData2 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, 2, 2))
    val valueData3 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, 3, 3))
    val valueDataTemp1 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, 4, 4))
    val valueDataTemp2 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, 5, 5))
    val valueDataTemp3 = DataSources.fromNumericCellRange(sheet, CellRangeAddress(15, lastRowIndex, 6, 6))

    var start = 16
    val size = 25
    val space = 4
    val lineChart1 = createLineChart(sheet, start, start + size)
    drawLineChart3(lineChart1, timeData1, valueData1, "Начало(ДТВ1), минут", "Влажность, %")
    start += size + space
    val lineChart2 = createLineChart(sheet, start, start + size)
    drawLineChart3(lineChart2, timeData1, valueData2, "Середина(ДТВ2), минут", "Влажность, %")
    start += size + space
    val lineChart3 = createLineChart(sheet, start, start + size)
    drawLineChart3(lineChart3, timeData1, valueData3, "Конец(ДТВ3), минут", "Влажность, %")
    start += size + space
    val lineChartTemp1 = createLineChart(sheet, start, start + size)
    drawLineChart3(lineChartTemp1, timeData1, valueDataTemp1, "Начало(ДТВ1), минут", "Температура, °C")
    start += size + space + 13
    val lineChartTemp2 = createLineChart(sheet, start, start + size)
    drawLineChart3(lineChartTemp2, timeData1, valueDataTemp2, "Середина(ДТВ2), минут", "Температура, °C")
    start += size + space
    val lineChartTemp3 = createLineChart(sheet, start, start + size)
    drawLineChart3(lineChartTemp3, timeData1, valueDataTemp3, "Конец(ДТВ3), минут", "Температура, °C")
}

private fun createLineChart(sheet: XSSFSheet, rowStart: Int, rowEnd: Int): XSSFChart {
    val drawing = sheet.createDrawingPatriarch()
    val anchor = drawing.createAnchor(0, 0, 0, 0, 7, rowStart, 36, rowEnd)

    return drawing.createChart(anchor)
}

private fun drawLineChart3(
    lineChart: XSSFChart,
    xAxisData: ChartDataSource<Number>,
    yAxisData: ChartDataSource<Number>,
    section: String = "", title: String = ""
) {
    val data = lineChart.chartDataFactory.createLineChartData()

    val xAxis = lineChart.chartAxisFactory.createCategoryAxis(AxisPosition.BOTTOM)
    val yAxis = lineChart.createValueAxis(AxisPosition.LEFT)
    yAxis.crosses = org.apache.poi.ss.usermodel.charts.AxisCrosses.AUTO_ZERO

    val series = data.addSeries(xAxisData, yAxisData)
    series.setTitle("График")
    lineChart.plot(data, xAxis, yAxis)
    lineChart.axes[0].setTitle(section)
    lineChart.axes[1].setTitle(title)

    val plotArea = lineChart.ctChart.plotArea
    plotArea.lineChartArray[0].smooth
    val ctBool = CTBoolean.Factory.newInstance()
    ctBool.`val` = false
    plotArea.lineChartArray[0].smooth = ctBool
    for (series in plotArea.lineChartArray[0].serArray) {
        series.smooth = ctBool
    }
}

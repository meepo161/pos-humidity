package ru.avem.poshumidity.utils

import ru.avem.poshumidity.database.entities.Protocol
import ru.avem.poshumidity.database.entities.ProtocolSingle
import ru.avem.poshumidity.database.entities.TestObjectsType


object Singleton {
    var coef1: Int = 0
    var coef2: Int = 0
    var coef3: Int = 0
    lateinit var currentProtocol: Protocol
    lateinit var currentTestItem: TestObjectsType

    var listOfValuesTest1 = mutableListOf<String>()
    var listOfValuesTest2 = mutableListOf<String>()
    var listOfValuesTest3 = mutableListOf<String>()

    var listOfValuesTempTest1 = mutableListOf<String>()
    var listOfValuesTempTest2 = mutableListOf<String>()
    var listOfValuesTempTest3 = mutableListOf<String>()

    var minIndex1 = 0.0
    var minIndex2 = 0.0
    var minIndex3 = 0.0
    var maxIndex1 = 0.0
    var maxIndex2 = 0.0
    var maxIndex3 = 0.0
    var minIndexTemp1 = 0.0
    var minIndexTemp2 = 0.0
    var minIndexTemp3 = 0.0
    var maxIndexTemp1 = 0.0
    var maxIndexTemp2 = 0.0
    var maxIndexTemp3 = 0.0
}

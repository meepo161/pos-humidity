package ru.avem.poshumidity.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ProtocolsTable : IntIdTable() {
    val date = varchar("date", 256)
    val time = varchar("time", 256)
    val dateEnd = varchar("dateEnd", 256)
    val timeEnd = varchar("timeEnd", 256)
    val cipher1 = varchar("cipher1", 256)
    val productNumber1 = varchar("productNumber1", 256)
    val operator = varchar("operator", 256)
    val values1 =  varchar("values1", 99999999)
    val values2 =  varchar("values2", 99999999)
    val values3 =  varchar("values3", 99999999)
    val valuesTemp1 =  varchar("valuesTemp1", 99999999)
    val valuesTemp2 =  varchar("valuesTemp2", 99999999)
    val valuesTemp3 =  varchar("valuesTemp3", 99999999)
    var NUMBER_DATE_ATTESTATION = varchar("NUMBER_DATE_ATTESTATION", 512)
    var NAME_OF_OPERATION = varchar("NAME_OF_OPERATION", 512)
    var NUMBER_CONTROLLER = varchar("NUMBER_CONTROLLER", 512)
}

class Protocol(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Protocol>(ProtocolsTable)

    var date by ProtocolsTable.date
    var time by ProtocolsTable.time
    var dateEnd by ProtocolsTable.dateEnd
    var timeEnd by ProtocolsTable.timeEnd
    var cipher1 by ProtocolsTable.cipher1
    var operator by ProtocolsTable.operator
    var productNumber1 by ProtocolsTable.productNumber1
    var values1 by ProtocolsTable.values1
    var values2 by ProtocolsTable.values2
    var values3 by ProtocolsTable.values3
    var valuesTemp1 by ProtocolsTable.valuesTemp1
    var valuesTemp2 by ProtocolsTable.valuesTemp2
    var valuesTemp3 by ProtocolsTable.valuesTemp3
    var NUMBER_DATE_ATTESTATION by ProtocolsTable.NUMBER_DATE_ATTESTATION
    var NAME_OF_OPERATION by ProtocolsTable.NAME_OF_OPERATION
    var NUMBER_CONTROLLER by ProtocolsTable.NUMBER_CONTROLLER

    override fun toString(): String {
        return "$id"
    }
}
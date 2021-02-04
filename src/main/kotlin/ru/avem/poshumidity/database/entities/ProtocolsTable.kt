package ru.avem.poshumidity.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ProtocolsTable : IntIdTable() {
    val date = varchar("date", 256)
    val time = varchar("time", 256)
    val values1 =  varchar("values1", 99999999)
    val values2 =  varchar("values2", 99999999)
    val values3 =  varchar("values3", 99999999)
}

class Protocol(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Protocol>(ProtocolsTable)

    var date by ProtocolsTable.date
    var time by ProtocolsTable.time
    var values1 by ProtocolsTable.values1
    var values2 by ProtocolsTable.values2
    var values3 by ProtocolsTable.values3

    override fun toString(): String {
        return "$id"
    }
}
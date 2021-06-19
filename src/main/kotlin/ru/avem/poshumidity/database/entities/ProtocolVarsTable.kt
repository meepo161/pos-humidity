package ru.avem.poshumidity.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ProtocolVarsTable : IntIdTable() {
    var NUMBER_DATE_ATTESTATION = varchar("NUMBER_DATE_ATTESTATION", 512)
    var NAME_OF_OPERATION = varchar("NAME_OF_OPERATION", 512)
    var NUMBER_CONTROLLER = varchar("NUMBER_CONTROLLER", 512)
}

class ProtocolVars(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProtocolVars>(ProtocolVarsTable)
    var NUMBER_DATE_ATTESTATION by ProtocolVarsTable.NUMBER_DATE_ATTESTATION
    var NAME_OF_OPERATION by ProtocolVarsTable.NAME_OF_OPERATION
    var NUMBER_CONTROLLER by ProtocolVarsTable.NUMBER_CONTROLLER

    override fun toString(): String {
        return id.toString()
    }
}

package ru.avem.poshumidity.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object CoefValuesTable : IntIdTable() {
    var COEF1 = varchar("COEF1", 512)
    var COEF2 = varchar("COEF2", 512)
    var COEF3 = varchar("COEF3", 512)
}

class CoefValues(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CoefValues>(CoefValuesTable)

    var COEF1 by CoefValuesTable.COEF1
    var COEF2 by CoefValuesTable.COEF2
    var COEF3 by CoefValuesTable.COEF3

    override fun toString(): String {
        return id.toString()
    }
}

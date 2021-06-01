package ru.avem.poshumidity.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Users : IntIdTable() {
    val password = varchar("pass", 64)
    val fullName = varchar("fullname", 128)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var password by Users.password
    var fullName by Users.fullName

    override fun toString(): String {
        return fullName
    }
}

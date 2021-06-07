package ru.avem.poshumidity.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.poshumidity.database.entities.*
import ru.avem.poshumidity.database.entities.Users.fullName
import ru.avem.poshumidity.utils.formatRealNumber
import java.sql.Connection

fun validateDB() {
    Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        SchemaUtils.create(Users, ProtocolsTable, ProtocolsSingleTable, ObjectsTypes)
    }

    transaction {
        if (User.all().count() < 2) {
            val admin = User.find {
                fullName eq "admin"
            }

            if (admin.empty()) {
                User.new {
                    password = "avem"
                    fullName = "admin"
                }
            }

            if (TestObjectsType.all().count() < 1) {
                TestObjectsType.new {
                    serialNumber = "111111"
                    resistanceCoil = "0.1"
                    resistanceContactGroup = "0.2"
                    voltageMin = "0.3"
                    voltageMax = "0.4"
                    timeOff = "0.5"
                }

                TestObjectsType.new {
                    serialNumber = "222222"
                    resistanceCoil = "1.1"
                    resistanceContactGroup = "1.2"
                    voltageMin = "1.3"
                    voltageMax = "1.4"
                    timeOff = "1.5"
                }

                TestObjectsType.new {
                    serialNumber = "3333333"
                    resistanceCoil = "2.1"
                    resistanceContactGroup = "2.2"
                    voltageMin = "2.3"
                    voltageMax = "2.4"
                    timeOff = "2.5"
                }

                Protocol.new {
                    date = "10.03.2020"
                    time = "11:30:00"
                    cipher1 = "111111111"
                    productNumber1 = "777777"
                    operator = "Иванов И.И."
                    val list1 = mutableListOf<String>()
                    val list2 = mutableListOf<String>()
                    val list3 = mutableListOf<String>()
                    for (i in 0..20000) {
                        list1.add("97")
                        list2.add(formatRealNumber(98 + Math.random()).toString())
                        list3.add(formatRealNumber(96 + Math.random() * 2).toString())
                    }
                    val listTemp1 = mutableListOf<String>()
                    val listTemp2 = mutableListOf<String>()
                    val listTemp3 = mutableListOf<String>()
                    for (i in 0..20000) {
                        listTemp1.add(formatRealNumber(23 + Math.random()).toString())
                        listTemp2.add(formatRealNumber(35 + Math.random()).toString())
                        listTemp3.add(formatRealNumber(27 + Math.random() * 2).toString())
                    }
                    values1 = list1.toString()
                    values2 = list2.toString()
                    values3 = list3.toString()
                    valuesTemp1 = listTemp1.toString()
                    valuesTemp2 = listTemp2.toString()
                    valuesTemp3 = listTemp3.toString()
                }

                ProtocolSingle.new {
                    date = "10.03.2020"
                    time = "11:30:00"
                    values = "[0,1, 0,2, 0,4, 0,8, 1,6, 3,2, 6,4, 12,8, 25,6, 51,2, 102,4, 204,8]"
                }
            }
        }
    }
}

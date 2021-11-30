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
        SchemaUtils.create(Users, ProtocolsTable, ProtocolsSingleTable, ObjectsTypes, ProtocolVarsTable, CoefValuesTable)
    }

    transaction {
        if (User.all().count() < 1) {
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
                ProtocolVars.new {
                    NUMBER_DATE_ATTESTATION = "номер и дата аттестации"
                    NAME_OF_OPERATION = "Наименование и шифр технологического процесса"
                    NUMBER_CONTROLLER = "1"
                }

                Protocol.new {
                    date = "10.03.2020"
                    time = "11:30:00"
                    dateEnd = "10.03.2021"
                    timeEnd = "12:33:33"
                    cipher1 = "111111111"
                    productNumber1 = "777777"
                    operator = "Иванов И.И."
                    val list1 = mutableListOf<String>()
                    val list2 = mutableListOf<String>()
                    val list3 = mutableListOf<String>()
                    for (i in 0..172800) {
                        list1.add(formatRealNumber(97 + Math.random()).toString())
                        list2.add(formatRealNumber(98 + Math.random()).toString())
                        list3.add(formatRealNumber(96 + Math.random() * 15).toString())
                    }
                    val listTemp1 = mutableListOf<String>()
                    val listTemp2 = mutableListOf<String>()
                    val listTemp3 = mutableListOf<String>()
                    for (i in 0..172800) {
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
                    NUMBER_DATE_ATTESTATION = "номер и дата аттестации"
                    NAME_OF_OPERATION = "Наименование и шифр технологического процесса"
                    NUMBER_CONTROLLER = "1"
                }

                ProtocolSingle.new {
                    date = "10.03.2020"
                    time = "11:30:00"
                    values = "[0,1, 0,2, 0,4, 0,8, 1,6, 3,2, 6,4, 12,8, 25,6, 51,2, 102,4, 204,8]"
                }

                CoefValues.new {
                    COEF1 = "3"
                    COEF2 = "4"
                    COEF3 = "5"
                }
            }
        }
    }
}

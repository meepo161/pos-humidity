package ru.avem.poshumidity.entities

import javafx.beans.property.StringProperty

data class TableValuesTest(
        var descriptor: StringProperty,
        var humidity: StringProperty,
        var temperature: StringProperty,
        var generator: StringProperty
)

data class TableValuesTest1(
        var descriptor: StringProperty,
        var section1t: StringProperty
)

data class TableValuesTest2(
        var descriptor: StringProperty,
        var section21t: StringProperty
)

data class TableValuesTest3(
        var descriptor: StringProperty,
        var section31t: StringProperty
)

data class TableValuesTestTime(
        var descriptor: StringProperty,
        var start: StringProperty,
        var pause: StringProperty
)
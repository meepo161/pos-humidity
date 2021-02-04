package ru.avem.poshumidity.communication.model.devices.dtv

import ru.avem.poshumidity.communication.model.DeviceRegister
import ru.avem.poshumidity.communication.model.IDeviceModel

class Dtv02Model : IDeviceModel {
    companion object {
        const val DEWPOINT = "DEWPOINT"
        const val HUMIDITY = "HUMIDITY"
        const val TEMPERATURE = "TEMPERATURE"
    }

    override val registers: Map<String, DeviceRegister> = mapOf(
        TEMPERATURE to DeviceRegister(0x0151, DeviceRegister.RegisterValueType.FLOAT),
        DEWPOINT to DeviceRegister(0x0155, DeviceRegister.RegisterValueType.FLOAT),
        HUMIDITY to DeviceRegister(0x0153, DeviceRegister.RegisterValueType.FLOAT)
    )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")

    var outMask: Short = 0
}
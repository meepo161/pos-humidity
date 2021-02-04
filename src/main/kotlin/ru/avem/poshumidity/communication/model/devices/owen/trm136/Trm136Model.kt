package ru.avem.poshumidity.communication.model.devices.owen.trm136

import ru.avem.poshumidity.communication.model.DeviceRegister
import ru.avem.poshumidity.communication.model.IDeviceModel

class Trm136Model : IDeviceModel {
    companion object {
        const val TEMPERATURE_1 = "TEMPERATURE_1"
        const val TEMPERATURE_2 = "TEMPERATURE_2"
        const val TEMPERATURE_3 = "TEMPERATURE_3"
        const val TEMPERATURE_4 = "TEMPERATURE_4"
        const val TEMPERATURE_5 = "TEMPERATURE_5"
        const val TEMPERATURE_6 = "TEMPERATURE_6"
        const val TEMPERATURE_7 = "TEMPERATURE_7"
        const val TEMPERATURE_8 = "TEMPERATURE_8"
    }

    override val registers: Map<String, DeviceRegister> = mapOf(
            TEMPERATURE_1 to DeviceRegister(0x0003, DeviceRegister.RegisterValueType.FLOAT),
            TEMPERATURE_2 to DeviceRegister(0x0008, DeviceRegister.RegisterValueType.FLOAT),
            TEMPERATURE_3 to DeviceRegister(0x000D, DeviceRegister.RegisterValueType.FLOAT),
            TEMPERATURE_4 to DeviceRegister(0x0012, DeviceRegister.RegisterValueType.FLOAT),
            TEMPERATURE_5 to DeviceRegister(0x0017, DeviceRegister.RegisterValueType.FLOAT),
            TEMPERATURE_6 to DeviceRegister(0x001C, DeviceRegister.RegisterValueType.FLOAT),
            TEMPERATURE_7 to DeviceRegister(0x0021, DeviceRegister.RegisterValueType.FLOAT),
            TEMPERATURE_8 to DeviceRegister(0x0026, DeviceRegister.RegisterValueType.FLOAT)
    )

    override fun getRegisterById(idRegister: String) =
            registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")

    var outMask: Short = 0
}
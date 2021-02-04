package ru.avem.poshumidity.communication.model.devices.parma

import ru.avem.poshumidity.communication.model.DeviceRegister
import ru.avem.poshumidity.communication.model.IDeviceModel

class ParmaModel : IDeviceModel {
    companion object {
        const val IA = "IA"
        const val IB = "IB"
        const val IC = "IC"
        const val UA = "UA"
        const val UB = "UB"
        const val UC = "UC"
        const val U_AB = "U_AB"
        const val U_BC = "U_BC"
        const val U_CA = "U_CA"
    }

    override val registers: Map<String, DeviceRegister> = mapOf(
        U_AB to DeviceRegister(4, DeviceRegister.RegisterValueType.SHORT, coefficient = 1.0 / 50),
        U_BC to DeviceRegister(5, DeviceRegister.RegisterValueType.SHORT, coefficient = 1.0 / 50),
        U_CA to DeviceRegister(6, DeviceRegister.RegisterValueType.SHORT, coefficient = 1.0 / 50),
        IA to DeviceRegister(7, DeviceRegister.RegisterValueType.SHORT, coefficient = 1.0 / 5000),
        IB to DeviceRegister(8, DeviceRegister.RegisterValueType.SHORT, coefficient = 1.0 / 5000),
        IC to DeviceRegister(9, DeviceRegister.RegisterValueType.SHORT, coefficient = 1.0 / 5000),
        UA to DeviceRegister(11, DeviceRegister.RegisterValueType.SHORT,coefficient = 1.0 / 100),
        UB to DeviceRegister(12, DeviceRegister.RegisterValueType.SHORT,coefficient = 1.0 / 100),
        UC to DeviceRegister(13, DeviceRegister.RegisterValueType.SHORT,coefficient = 1.0 / 100)
    )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")
}

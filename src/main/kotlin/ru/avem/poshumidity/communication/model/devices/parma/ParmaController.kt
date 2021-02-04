package ru.avem.poshumidity.communication.model.devices.parma

import ru.avem.kserialpooler.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.kserialpooler.communication.adapters.utils.ModbusRegister
import ru.avem.poshumidity.communication.model.DeviceRegister
import ru.avem.poshumidity.communication.model.IDeviceController
import ru.avem.poshumidity.communication.utils.TransportException

class ParmaController(
    override val name: String,
    override val protocolAdapter: ModbusRTUAdapter,
    override val id: Byte
) : IDeviceController {
    val model = ParmaModel()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()

    companion object {
        val PARMA_NAN = 0xFFFF.toShort()
    }

    override fun readRegister(register: DeviceRegister) {
        isResponding = try {
            transactionWithAttempts {
                val modbusRegister =
                    protocolAdapter.readInputRegisters(id, register.address, 1).map(ModbusRegister::toShort)
                val value = modbusRegister.first()
                if (value != PARMA_NAN) {
                    register.value = value
                } else {
                    register.value = Double.NaN
                }
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun <T : Number> writeRegister(register: DeviceRegister, value: T) {

    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
        val registers = values.map { ModbusRegister(it) }
        isResponding = try {
            transactionWithAttempts {
                protocolAdapter.presetMultipleRegisters(id, register.address, registers)
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun checkResponsibility() {
        model.registers.values.firstOrNull()?.let {
            readRegister(it)
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)

    fun readIA(): Double {
        return getRegisterById(ParmaModel.IA).value.toDouble() / 5000.0
    }

    fun readIB(): Double {
        return getRegisterById(ParmaModel.IB).value.toDouble() / 5000.0
    }

    fun readIC(): Double {
        return getRegisterById(ParmaModel.IC).value.toDouble() / 5000.0
    }

    fun readUA(): Double {
        return getRegisterById(ParmaModel.UA).value.toDouble() / 100.0
    }

    fun readUB(): Double {
        return getRegisterById(ParmaModel.UB).value.toDouble() / 100.0
    }

    fun readUC(): Double {
        return getRegisterById(ParmaModel.UC).value.toDouble() / 100.0
    }
}

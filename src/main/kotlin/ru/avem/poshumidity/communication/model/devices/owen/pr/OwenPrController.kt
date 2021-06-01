package ru.avem.poshumidity.communication.model.devices.owen.pr

import ru.avem.kserialpooler.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.kserialpooler.communication.adapters.utils.ModbusRegister
import ru.avem.poshumidity.communication.model.DeviceRegister
import ru.avem.poshumidity.communication.model.IDeviceController
import ru.avem.poshumidity.utils.sleep
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.pow

class OwenPrController(
    override val name: String,
    override val protocolAdapter: ModbusRTUAdapter,
    override val id: Byte
) : IDeviceController {
    val model = OwenPrModel()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()

    var outMask: Short = 0
    var outMask2: Short = 0
    var outMask3: Short = 0

    companion object {
        const val TRIG_RESETER: Short = 0xFFFF.toShort()
        const val WD_RESETER: Short = 0b10
    }

    override fun readRegister(register: DeviceRegister) {
        isResponding = try {
            transactionWithAttempts {
                val modbusRegister =
                    protocolAdapter.readHoldingRegisters(id, register.address, 1).map(ModbusRegister::toShort)
                register.value = modbusRegister.first()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun <T : Number> writeRegister(register: DeviceRegister, value: T) {
        isResponding = try {
            when (value) {
                is Float -> {
                    val bb = ByteBuffer.allocate(4).putFloat(value).order(ByteOrder.LITTLE_ENDIAN)
                    val registers = listOf(ModbusRegister(bb.getShort(2)), ModbusRegister(bb.getShort(0)))
                    transactionWithAttempts {
                        protocolAdapter.presetMultipleRegisters(id, register.address, registers)
                    }
                }
                is Int -> {
                    val bb = ByteBuffer.allocate(4).putInt(value).order(ByteOrder.LITTLE_ENDIAN)
                    val registers = listOf(ModbusRegister(bb.getShort(2)), ModbusRegister(bb.getShort(0)))
                    transactionWithAttempts {
                        protocolAdapter.presetMultipleRegisters(id, register.address, registers)
                    }
                }
                is Short -> {
                    transactionWithAttempts {
                        protocolAdapter.presetSingleRegister(id, register.address, ModbusRegister(value))
                    }
                }
                else -> {
                    throw UnsupportedOperationException("Method can handle only with Float, Int and Short")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
        val registers = values.map { ModbusRegister(it) }
        isResponding = try {
            transactionWithAttempts {
                protocolAdapter.presetMultipleRegisters(id, register.address, registers)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun checkResponsibility() {
        model.registers.values.firstOrNull()?.let {
            readRegister(it)
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)

    private fun onBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask = outMask or 2.0.pow(nor).toInt().toShort()
        writeRegister(register, outMask)
        sleep(300)
    }

    private fun onBitInRegister2(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask2 = outMask2 or 2.0.pow(nor).toInt().toShort()
        writeRegister(register, outMask2)
        sleep(300)
    }

    private fun onBitInRegister3(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask3 = outMask3 or 2.0.pow(nor).toInt().toShort()
        writeRegister(register, outMask3)
        sleep(300)
    }

    private fun offBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask = outMask and 2.0.pow(nor).toInt().inv().toShort()
        writeRegister(register, outMask)
        sleep(300)
    }

    private fun offBitInRegister2(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask2 = outMask2 and 2.0.pow(nor).toInt().inv().toShort()
        writeRegister(register, outMask2)
        sleep(300)
    }

    private fun offBitInRegister3(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask3 = outMask3 and 2.0.pow(nor).toInt().inv().toShort()
        writeRegister(register, outMask3)
        sleep(300)
    }


    fun initOwenPR() {
        writeRegister(getRegisterById(OwenPrModel.RES_REGISTER), 1)
        writeRegister(getRegisterById(OwenPrModel.RES_REGISTER), 0)
    }

    fun resetKMS() {
        writeRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 0)
        writeRegister(getRegisterById(OwenPrModel.KMS2_REGISTER), 0)
        writeRegister(getRegisterById(OwenPrModel.KMS3_REGISTER), 0)
    }

    fun on1() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 1)
    }

    fun on2() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 2)
    }

    fun on3() {
        onBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 3)
    }

    fun onSound() {
        onBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 8)
    }

    fun off1() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 1)
    }

    fun off2() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 2)
    }

    fun off3() {
        offBitInRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 3)
    }

    fun offSound() {
        offBitInRegister2(getRegisterById(OwenPrModel.KMS2_REGISTER), 8)
    }

    fun offAllKMs() {
        writeRegister(getRegisterById(OwenPrModel.KMS1_REGISTER), 0)
        writeRegister(getRegisterById(OwenPrModel.KMS2_REGISTER), 0)
        writeRegister(getRegisterById(OwenPrModel.KMS3_REGISTER), 0)
        outMask = 0
        outMask2 = 0
        outMask3 = 0
    }
}

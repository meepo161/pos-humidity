package ru.avem.poshumidity.communication.model

import ru.avem.kserialpooler.communication.Connection
import ru.avem.kserialpooler.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.kserialpooler.communication.utils.SerialParameters
import ru.avem.poshumidity.app.Pos.Companion.isAppRunning
import ru.avem.poshumidity.communication.model.devices.dtv.Dtv02Controller
import ru.avem.poshumidity.communication.model.devices.owen.pr.OwenPrController
import ru.avem.poshumidity.communication.model.devices.owen.trm136.Trm136Controller
import ru.avem.poshumidity.communication.model.devices.parma.ParmaController
import java.lang.Thread.sleep
import kotlin.concurrent.thread

object CommunicationModel {
    @Suppress("UNUSED_PARAMETER")
    enum class DeviceID(description: String) {
        DD2("ПР"),
        DTV1("ДТВ"),
        DTV2("ДТВ"),
        DTV3("ДТВ")
    }

    var isConnected = false

    val connection = Connection(
            adapterName = "CP2103 USB to RS-485",
            serialParameters = SerialParameters(8, 0, 1, 9600),
            timeoutRead = 100,
            timeoutWrite = 100
    ).apply {
        connect()
        isConnected = true
    }

    private val modbusAdapter = ModbusRTUAdapter(connection)

    private val deviceControllers: Map<DeviceID, IDeviceController> = mapOf(
            DeviceID.DD2 to OwenPrController(DeviceID.DD2.toString(), modbusAdapter, 1),
            DeviceID.DTV1 to Dtv02Controller(DeviceID.DTV1.toString(), modbusAdapter, 2),
            DeviceID.DTV2 to Dtv02Controller(DeviceID.DTV2.toString(), modbusAdapter, 3),
            DeviceID.DTV3 to Dtv02Controller(DeviceID.DTV3.toString(), modbusAdapter, 4)
    )

    init {
        thread(isDaemon = true) {
            while (isAppRunning) {
                if (isConnected) {
                    deviceControllers.values.forEach {
                        it.readPollingRegisters()
                    }
                }
                sleep(100)
            }
        }
        thread(isDaemon = true) {
            while (isAppRunning) {
                if (isConnected) {
                    deviceControllers.values.forEach {
                        it.writeWritingRegisters()
                    }
                }
                sleep(100)
            }
        }
    }

    fun getDeviceById(deviceID: DeviceID) = deviceControllers[deviceID] ?: error("Не определено $deviceID")

    fun startPoll(deviceID: DeviceID, registerID: String, block: (Number) -> Unit) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        register.addObserver { _, arg ->
            block(arg as Number)
        }
        device.addPollingRegister(register)
    }

    fun clearPollingRegisters() {
        deviceControllers.values.forEach(IDeviceController::removeAllPollingRegisters)
        deviceControllers.values.forEach(IDeviceController::removeAllWritingRegisters)
    }

    fun removePollingRegister(deviceID: DeviceID, registerID: String) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        register.deleteObservers()
        device.removePollingRegister(register)
    }

    fun checkDevices(): List<DeviceID> {
        deviceControllers.values.forEach(IDeviceController::checkResponsibility)
        return deviceControllers.filter { !it.value.isResponding }.keys.toList()
    }

    fun addWritingRegister(deviceID: DeviceID, registerID: String, value: Number) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        device.addWritingRegister(register to value)
    }
}

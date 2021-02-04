package ru.avem.poshumidity.controllers

import ru.avem.poshumidity.communication.model.CommunicationModel
import ru.avem.poshumidity.communication.model.CommunicationModel.getDeviceById
import ru.avem.poshumidity.communication.model.devices.dtv.Dtv02Controller
import ru.avem.poshumidity.communication.model.devices.owen.pr.OwenPrController
import ru.avem.poshumidity.communication.model.devices.owen.trm136.Trm136Controller
import ru.avem.poshumidity.communication.model.devices.parma.ParmaController
import tornadofx.Controller

abstract class TestController : Controller() {
    protected val owenPR = getDeviceById(CommunicationModel.DeviceID.DD2) as OwenPrController
    protected val dtv1 = getDeviceById(CommunicationModel.DeviceID.DTV1) as Dtv02Controller
    protected val dtv2 = getDeviceById(CommunicationModel.DeviceID.DTV2) as Dtv02Controller
    protected val dtv3 = getDeviceById(CommunicationModel.DeviceID.DTV3) as Dtv02Controller

}

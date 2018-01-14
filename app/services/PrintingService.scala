package services

import javax.inject.{Inject, Singleton}

import com.zebra.sdk.comm.{ConnectionException, TcpConnection}
import com.zebra.sdk.printer.discovery.{DiscoveredPrinter, DiscoveryHandler}
import models.order.FyndiqOrderResponse
import play.api.{Configuration, Logger}

@Singleton
class PrintingService @Inject()(conf: Configuration) {

  def printAddressLabel(address: FyndiqOrderResponse): Unit = {
    val printerIp = conf.getOptional[String]("zebra.ip").getOrElse("")
    val thePrinterConn = new TcpConnection(printerIp, TcpConnection.DEFAULT_ZPL_TCP_PORT)
    val logger = Logger(classOf[PrintingService])

    try {
      thePrinterConn.open()
      val zplData =
        s"""
           |^XA
           |^CFA,20
           |^FO50,50^FD${address.delivery_firstname} ${address.delivery_lastname}^FS
           |^FO50,90^FD${address.delivery_address}^FS
           |^FO50,130^FD${address.delivery_postalcode} ${address.delivery_city}^FS
           |^FO50,170^FDUnited States (USA)^FS
           |^XZ
         """.stripMargin

      thePrinterConn.write(zplData.getBytes)
    } catch {
      case e: ConnectionException => logger.error(e.getMessage)
      case unknown => logger.error(unknown.getMessage)
    } finally {
      thePrinterConn.close()
    }
  }

}

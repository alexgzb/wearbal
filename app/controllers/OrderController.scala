package controllers

import javax.inject.{Inject, Singleton}

import com.netaporter.uri.Uri
import models.order.FyndiqOrderResponse
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.PrintingService

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(val controllerComponents: ControllerComponents,
                                val wSClient: WSClient,
                                val printingService: PrintingService)
                               (implicit executionContext: ExecutionContext) extends BaseController{

  def printOrder(orderNr: String): Action[AnyContent] = Action.async {
    val url = Uri.parse(s"https://fyndiq.se/api/v1/order/$orderNr").addParams(Seq(
      "token" -> "pihLrGOrnQRlEG2ascwElkuOA480v86q",
      "user" -> "niclas.jansson@oddhabit.com"
    ))

    wSClient.url(url.toString).get().map(_.json.as[FyndiqOrderResponse]).map { s =>
    printingService.printAddressLabel(s)
      Redirect("Printing")
      //Ok(s"${s.delivery_firstname} ${s.delivery_lastname}\n\n${s.delivery_address}\n\n${s.delivery_postalcode} ${s.delivery_city}")
    }
  }
}

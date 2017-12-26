package controllers

import javax.inject.{Inject, Singleton}

import com.netaporter.uri.Uri
import internal.FyndiqProductsResponse
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext


@Singleton
class PopulateFromFyndiqController @Inject()(wSClient: WSClient)(implicit executionContext: ExecutionContext) extends Controller {

  def getProducts: Action[AnyContent] = Action.async {
    val url = Uri.parse("https://fyndiq.se/api/v1/product/").addParams(Seq(
      "token" -> "pihLrGOrnQRlEG2ascwElkuOA480v86q",
      "user" -> "niclas.jansson@oddhabit.com",
      "limit" -> "1000"
    ))
//    wSClient.url(url.toString()).get().map(_.json.as[FyndiqProductsResponse]).map(s => Ok(s.toString))
    val obj = wSClient.url(url.toString()).get().map(_.json.as[FyndiqProductsResponse]).map(s => s.objects)
    val kalle = obj.map(s => s.flatMap(p => p.images))
    kalle.map(p => Redirect(p.head))
  }
}

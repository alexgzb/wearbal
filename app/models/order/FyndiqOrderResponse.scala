package models.order

import play.api.libs.json.{Json, Reads}

case class FyndiqOrderResponse(delivery_address: String,
                               delivery_postalcode: String,
                               delivery_city: String,
                               delivery_firstname: String,
                               delivery_lastname: String)

object FyndiqOrderResponse {
  implicit val fyndiqOrderResponseFormat: Reads[FyndiqOrderResponse] = Json.reads[FyndiqOrderResponse]
}
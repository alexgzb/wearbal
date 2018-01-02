package models

import play.api.libs.json.{Json, OFormat}

case class Variation(id: Int, merchant_item_no: String, merchant_location: String, name: String, num_in_stock: Int, platform_item_no: String)

object Variation {
  implicit val variationFormats: OFormat[Variation] = Json.format[Variation]
}
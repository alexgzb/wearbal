package internal

import play.api.libs.json.Json

case class FyndiqProductsResponse(meta: Meta, objects: List[Produkt])

object FyndiqProductsResponse {
  implicit val fyndiqProductsResponseReads = Json.reads[FyndiqProductsResponse]
  implicit val fyndiqProductsResponseWritable = Json.writes[FyndiqProductsResponse]
//  implicit val fyndiqProductsResponseReads: Reads[FyndiqProductsResponse] = (
//    (JsPath \ "meta").read[Meta] and
//    (JsPath \ "objects").read[Seq[Produkt]]
//    )(FyndiqProductsResponse.apply _)

}
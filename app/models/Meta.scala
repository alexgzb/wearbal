package internal

import play.api.libs.json.Json

case class Meta(limit: Int, next: Option[String], offset: Int, previous: Option[String], total_count: Int)

object Meta {
  implicit val metaReads = Json.format[Meta]
//  implicit val metaReads: Reads[Meta] = (
//    (JsPath \ "limit").read[Int] and
//      (JsPath \ "next").readNullable[String] and
//      (JsPath \ "offset").read[Int] and
//      (JsPath \ "previous").readNullable[String] and
//      (JsPath \ "total_count").read[Int]
//    )(Meta.apply _)
}

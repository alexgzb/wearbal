package internal

import models.VariationGroup
import play.api.libs.json.{Json, OFormat}

case class FyndiqProduct(description: String,
                         id: Int,
                         images: List[String],
                         is_blocked_by_fyndiq: Boolean,
                         item_no: String,
                         moms_percent: Int,
                         num_in_stock: Int,
                         oldprice: String,
                         price: String,
                         resource_uri: String,
                         state: String,
                         title: String,
                         url: String,
                         variation_group: Option[List[VariationGroup]]
                 ) {

  override def toString: String = {
    s"\n\n$title" +
      s";$id" +
      s";$item_no" +
      s";$num_in_stock" +
      s";$price"
  }

//
//  override def toString: String = {
//    //s"Description: $description" +
//    s"\n\nTitle: $title" +
//      //s"\n\nImages: $images" +
//        s"\t\tId: $id" +
//        s"\t\tItem_no: $item_no" +
//        s"\t\tNum in Stock: $num_in_stock" +
//        s"\t\tPrice: $price" +
//        //s"\t\tResource Uri: $resource_uri" +
//      s"\t\tUrl: $url\n\n"
//  }
}
object FyndiqProduct {
  implicit val produktReads: OFormat[FyndiqProduct] = Json.format[FyndiqProduct]
//  implicit val objectReads: Reads[Product] = (
//    (JsPath \ "description").read[String] and
//    (JsPath \ "id").read[String] and
//    (JsPath \ "images").read[Seq[String]] and
//    (JsPath \ "is_blocked_by_fyndiq").read[Boolean] and
//    (JsPath \ "item_no").read[String] and
//    (JsPath \ "moms_percent").read[Int] and
//    (JsPath \ "num_in_stock").read[Int] and
//    (JsPath \ "oldprice").read[Double] and
//    (JsPath \ "price").read[Double] and
//    (JsPath \ "resource_uri").read[String] and
//    (JsPath \ "state").read[String] and
//    (JsPath \ "title").read[String] and
//    (JsPath \ "url").read[String]
//    )(Product.apply _)
}

package models

import play.api.libs.json.{Json, OFormat}

case class VariationGroup(name: String, variations: List[Variation])

object VariationGroup {
    implicit val variationGroupFormats: OFormat[VariationGroup] = Json.format[VariationGroup]
}

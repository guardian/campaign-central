package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import cats.syntax.either._
import play.api.libs.json.{Format, JsValue, Json}

case class GraphDataPoint(name: String, dataPoint: Long, target: Option[Long]) {
  def toItem: Either[CampaignCentralApiError, Item] =
    Option(Item.fromJSON(Json.toJson(this).toString())).map(Right(_)) getOrElse Left(JsonParsingError(""))

}

object GraphDataPoint {
  implicit val GraphDataPointFormat: Format[GraphDataPoint] = Jsonx.formatCaseClass[GraphDataPoint]

  def fromJson(json: JsValue): Either[CampaignCentralApiError, GraphDataPoint] =
    json.asOpt[GraphDataPoint].map(Right(_)) getOrElse Left(JsonParsingError(""))
  def fromItem(item: Item): Either[CampaignCentralApiError, GraphDataPoint] =
    Either.catchNonFatal(Json.parse(item.toJSON).as[GraphDataPoint]).leftMap(e => JsonParsingError(e.getMessage))
}

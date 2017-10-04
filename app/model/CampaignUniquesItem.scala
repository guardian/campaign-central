package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import cats.syntax.either._
import play.api.libs.json.{Format, JsValue, Json}

case class CampaignUniquesItem(campaignId: String, reportExecutionTimestamp: String, uniques: Long) {
  def toItem: Either[CampaignCentralApiError, Item] =
    Option(Item.fromJSON(Json.toJson(this).toString())).map(Right(_)) getOrElse Left(JsonParsingError(""))
}

object CampaignUniquesItem {
  implicit val CampaignUniquesItemFormat: Format[CampaignUniquesItem] = Jsonx.formatCaseClass[CampaignUniquesItem]

  def fromJson(json: JsValue): Either[CampaignCentralApiError, CampaignUniquesItem] =
    json.asOpt[CampaignUniquesItem].map(Right(_)) getOrElse Left(JsonParsingError(""))
  def fromItem(item: Item): Either[CampaignCentralApiError, CampaignUniquesItem] =
    Either.catchNonFatal(Json.parse(item.toJSON).as[CampaignUniquesItem]).leftMap(e => JsonParsingError(e.getMessage))
}

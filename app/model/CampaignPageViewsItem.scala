package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.libs.json.{Format, JsValue, Json}
import cats.syntax.either._

case class CampaignPageViewsItem(campaignId: String,
                                 sortKey: String,
                                 pageviews: Long,
                                 reportExecutionTimestamp: String) {
  def toItem: Either[CampaignCentralApiError, Item] =
    Option(Item.fromJSON(Json.toJson(this).toString())).map(Right(_)) getOrElse Left(JsonParsingError(""))
}

object CampaignPageViewsItem {
  implicit val CampaignPageViewsItemFormat: Format[CampaignPageViewsItem] =
    Jsonx.formatCaseClass[CampaignPageViewsItem]

  def fromJson(json: JsValue): Either[CampaignCentralApiError, CampaignPageViewsItem] =
    json.asOpt[CampaignPageViewsItem].map(Right(_)) getOrElse Left(JsonParsingError(""))

  def fromItem(item: Item): Either[CampaignCentralApiError, CampaignPageViewsItem] =
    Either
      .catchNonFatal(Json.parse(item.toJSON).as[CampaignPageViewsItem])
      .leftMap(e => JsonParsingError(e.getMessage))
}

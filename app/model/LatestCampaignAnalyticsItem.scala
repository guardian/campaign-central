package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import cats.syntax.either._
import model.command.{CampaignCentralApiError, JsonParsingError}
import play.api.libs.json.{Format, JsValue, Json}

case class LatestCampaignAnalyticsItem(
  campaignId: String,
  uniques: Long,
  pageviews: Long,
  reportExecutionTimestamp: String,
  pageviewsByCountryCode: Map[String, Long],
  uniquesByCountryCode: Map[String, Long],
  pageviewsByDevice: Map[String, Long],
  uniquesByDevice: Map[String, Long],
  medianAttentionTimeSeconds: Option[Long],
  medianAttentionTimeByDevice: Option[Map[String, Long]],
  weightedAverageDwellTimeForCampaign: Option[Double],
  averageDwellTimePerPathSeconds: Option[Map[String, Double]]
) {
  def toItem: Either[CampaignCentralApiError, Item] =
    Option(Item.fromJSON(Json.toJson(this).toString())).map(Right(_)) getOrElse Left(JsonParsingError(""))
}

object LatestCampaignAnalyticsItem {

  implicit val LatestCampaignAnalyticsItemFormat: Format[LatestCampaignAnalyticsItem] =
    Jsonx.formatCaseClass[LatestCampaignAnalyticsItem]

  def fromJson(json: JsValue): Either[CampaignCentralApiError, LatestCampaignAnalyticsItem] =
    json.asOpt[LatestCampaignAnalyticsItem].map(Right(_)) getOrElse Left(JsonParsingError(""))
  def fromItem(item: Item): Either[CampaignCentralApiError, LatestCampaignAnalyticsItem] =
    Either
      .catchNonFatal(Json.parse(item.toJSON).as[LatestCampaignAnalyticsItem])
      .leftMap(e => JsonParsingError(e.getMessage))

}

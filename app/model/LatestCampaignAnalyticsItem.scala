package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.control.NonFatal

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
                                 medianAttentionTimeByDevice: Option[Map[String, Long]]
                               ){
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object LatestCampaignAnalyticsItem {
  implicit val LatestCampaignAnalyticsItemFormat: Format[LatestCampaignAnalyticsItem] = Jsonx.formatCaseClass[LatestCampaignAnalyticsItem]
  def fromJson(json: JsValue) = json.as[CampaignUniquesItem]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[LatestCampaignAnalyticsItem]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load campaignAnalyticsLatest item ${item.toJSON}", e)
      throw e
    }
  }
}

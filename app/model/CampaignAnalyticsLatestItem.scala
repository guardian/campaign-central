package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.control.NonFatal

case class CampaignAnalyticsLatestItem (
                                 campaignId: String,
                                 uniques: Long,
                                 pageviews: Long,
                                 reportExecutionTimestamp: String
                               ){
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object CampaignAnalyticsLatestItem {
  implicit val CampaignAnalyticsLatestItemFormat: Format[CampaignAnalyticsLatestItem] = Jsonx.formatCaseClass[CampaignAnalyticsLatestItem]
  def fromJson(json: JsValue) = json.as[CampaignUniquesItem]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[CampaignAnalyticsLatestItem]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load campaignAnalyticsLatest item ${item.toJSON}", e)
      throw e
    }
  }
}

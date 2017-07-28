package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.control.NonFatal

case class CampaignPageViewsItem (
  campaignId: String,
  startDate:Long,
  endDate:Long,
  reportExecutionTimestamp: String,
  pageviews: Long
){
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object CampaignPageViewsItem {
  implicit val CampaignPageViewsItemFormat: Format[CampaignPageViewsItem] = Jsonx.formatCaseClass[CampaignPageViewsItem]
  def fromJson(json: JsValue) = json.as[CampaignPageViewsItem]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[CampaignPageViewsItem]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load content item ${item.toJSON}", e)
      throw e
    }
  }
}


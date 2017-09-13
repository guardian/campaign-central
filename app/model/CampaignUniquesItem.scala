package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.control.NonFatal

case class CampaignUniquesItem(
  campaignId: String,
  reportExecutionTimestamp: String,
  uniques: Long
) {
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object CampaignUniquesItem {
  implicit val CampaignUniquesItemFormat: Format[CampaignUniquesItem] = Jsonx.formatCaseClass[CampaignUniquesItem]
  def fromJson(json: JsValue)                                         = json.as[CampaignUniquesItem]

  def fromItem(item: Item) =
    try {
      Json.parse(item.toJSON).as[CampaignUniquesItem]
    } catch {
      case NonFatal(e) => {
        Logger.error(s"failed to load content item ${item.toJSON}", e)
        throw e
      }
    }
}

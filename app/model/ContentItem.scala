package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.control.NonFatal

case class ContentItem(
                        campaignId: String,
                        id: String,
                        `type` : String,
                        composerId: Option[String],
                        path: Option[String],
                        title: String,
                        isLive: Boolean,
                        atoms: List[Atom] = Nil
                      ) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object ContentItem {
  implicit val contentItemFormat: Format[ContentItem] = Jsonx.formatCaseClass[ContentItem]

  def fromJson(json: JsValue) = json.as[ContentItem]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[ContentItem]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load content item ${item.toJSON}", e)
      throw e
    }
  }
}
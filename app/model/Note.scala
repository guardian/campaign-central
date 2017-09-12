package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.JodaReads._
import play.api.libs.json.JodaWrites._
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.control.NonFatal

case class Note(
  campaignId: String,
  created: DateTime,
  createdBy: User,
  lastModified: DateTime,
  lastModifiedBy: User,
  content: String
) {

  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object Note {
  implicit val noteFormat: Format[Note] = Jsonx.formatCaseClass[Note]

  def fromJson(json: JsValue) = json.as[Note]

  def fromItem(item: Item) =
    try {
      Json.parse(item.toJSON).as[Note]
    } catch {
      case NonFatal(e) => {
        Logger.error(s"failed to load note ${item.toJSON}", e)
        throw e
      }
    }
}

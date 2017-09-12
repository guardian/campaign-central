package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.control.NonFatal

case class Client(
  id: String,
  name: String,
  country: String,
  agency: Option[Agency] = None
) {
  def toItem = Item.fromJSON(Json.toJson(this).toString())

}

object Client {
  implicit val clientFormat: Format[Client] = Jsonx.formatCaseClass[Client]

  def fromJson(json: JsValue) = json.as[Client]

  def fromItem(item: Item) =
    try {
      Json.parse(item.toJSON).as[Client]
    } catch {
      case NonFatal(e) => {
        Logger.error(s"failed to load client ${item.toJSON}", e)
        throw e
      }
    }

}

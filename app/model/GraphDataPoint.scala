package model

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, JsValue, Json}

import scala.util.control.NonFatal


case class GraphDataPoint(name: String, dataPoint: Long, target: Long){
  def toItem = Item.fromJSON(Json.toJson(this).toString())
}

object GraphDataPoint {
  implicit val GraphDataPointFormat: Format[GraphDataPoint] = Jsonx.formatCaseClass[GraphDataPoint]
  def fromJson(json: JsValue) = json.as[GraphDataPoint]

  def fromItem(item: Item) = try {
    Json.parse(item.toJSON).as[GraphDataPoint]
  } catch {
    case NonFatal(e) => {
      Logger.error(s"failed to load GraphDataPoint ${item.toJSON}", e)
      throw e
    }
  }
}

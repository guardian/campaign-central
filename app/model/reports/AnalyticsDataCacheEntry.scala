package model.reports

import ai.x.play.json.Jsonx
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json.{Format, Json}
import util.Compression

import scala.util.control.NonFatal

case class AnalyticsDataCacheEntrySummary(key: String, dataType: String, expires: Option[Long], written: Long)

object AnalyticsDataCacheEntrySummary {
  implicit val analyticsDataCacheEntrySummaryFormat: Format[AnalyticsDataCacheEntrySummary] =
    Jsonx.formatCaseClass[AnalyticsDataCacheEntrySummary]

  def fromItem(item: Item) =
    try {
      Json.parse(item.toJSON).as[AnalyticsDataCacheEntrySummary]
    } catch {
      case NonFatal(e) => {
        Logger.error(s"failed to parse analytics data cache item summary ${item.toJSON}", e)
        throw e
      }
    }
}

case class AnalyticsDataCacheEntry(key: String, dataType: String, data: String, expires: Option[Long], written: Long) {
  //def toItem = Item.fromJSON(Json.toJson(this).toString())

  def toItem = {
    val item = new Item()
      .withString("key", key)
      .withString("dataType", dataType)
      .withBinary("compressedData", Compression.compress(data))
      .withLong("written", written)

    expires.foreach(item.withLong("expires", _))

    item
  }

}

object AnalyticsDataCacheEntry {
  implicit val analyticsDataCacheEntryFormat: Format[AnalyticsDataCacheEntry] =
    Jsonx.formatCaseClass[AnalyticsDataCacheEntry]

  def fromItem(item: Item) =
    try {
      if (item.isPresent("compressedData")) {
        AnalyticsDataCacheEntry(
          key = item.getString("key"),
          dataType = item.getString("dataType"),
          data = Compression.decompress(item.getBinary("compressedData")),
          expires = if (item.isPresent("expires")) Some(item.getLong("expires")) else None,
          written = item.getLong("written")
        )
      } else {
        Json.parse(item.toJSON).as[AnalyticsDataCacheEntry]
      }
    } catch {
      case NonFatal(e) => {
        Logger.error(s"failed to parse analytics data cache item ${item.toJSON}", e)
        throw e
      }
    }
}

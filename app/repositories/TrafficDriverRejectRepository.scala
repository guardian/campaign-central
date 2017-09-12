package repositories

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConverters._
import scala.util.control.NonFatal

object TrafficDriverRejectRepository {

  def getRejectedDriverIds(campaignId: String): Seq[Long] = {
    try {
      val items = Dynamo.trafficDriverRejectTable.query("campaignId", campaignId).asScala
      items.map(_.getNumber("lineItemId").longValue).toSeq
    } catch {
      case NonFatal(e) =>
        Logger.error(s"Failed to fetch rejected traffic drivers for campaign $campaignId", e)
        Nil
    }
  }

  def putRejectedDriverId(campaignId: String, lineItemId: Long): Unit = {
    try {
      val item = Item.fromMap(
        Map[String, AnyRef](
          "campaignId" -> campaignId,
          "lineItemId" -> long2Long(lineItemId)
        ).asJava)
      Dynamo.trafficDriverRejectTable.putItem(item)
    } catch {
      case NonFatal(e) =>
        Logger.error(s"Failed to store rejected traffic driver $lineItemId for campaign $campaignId", e)
    }
  }
}

package repositories

import com.amazonaws.services.dynamodbv2.document.ScanFilter
import model.CampaignUniquesItem
import org.joda.time.DateTime
import services.Dynamo
import scala.collection.JavaConverters._

import scala.collection.JavaConversions._

object CampaignUniquesRepository {

  private val ReportExecutionTimestampField = "reportExecutionTimestamp"

  def getCampaignUniques(campaignId: String): Seq[CampaignUniquesItem] = {
    Dynamo.campaignUniquesTable.query("campaignId", campaignId).map(CampaignUniquesItem.fromItem).toList
  }

  def getLatestCampaignUniques(): Seq[CampaignUniquesItem] = {
    val yesterday = DateTime.now.minusDays(1).toString("YYYY-MM-dd")
    val reportExecutionTimestampFilter: ScanFilter = new ScanFilter(ReportExecutionTimestampField).eq(yesterday)
    Dynamo.campaignUniquesTable.scan(reportExecutionTimestampFilter).asScala.toList.map(CampaignUniquesItem.fromItem)
  }

}

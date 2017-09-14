package repositories

import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.LatestCampaignAnalyticsItem
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

object LatestCampaignAnalyticsRepository {

  private implicit val logger: Logger = Logger(getClass)

  private val tableName = Config().latestCampaignAnalyticsTableName

  def getLatestCampaignAnalytics(): Seq[LatestCampaignAnalyticsItem] =
    getResults(Scanamo.scan[LatestCampaignAnalyticsItem](DynamoClient)(tableName))

  def getLatestCampaignAnalytics(campaignId: String): Option[LatestCampaignAnalyticsItem] = {
    val query = 'campaignId -> campaignId
    getResults(Scanamo.query[LatestCampaignAnalyticsItem](DynamoClient)(tableName)(query)).headOption
  }
}

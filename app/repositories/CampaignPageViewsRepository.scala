package repositories

import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.CampaignPageViewsItem
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

object CampaignPageViewsRepository {

  private implicit val logger: Logger = Logger(getClass)

  def getCampaignPageViews(campaignId: String): List[CampaignPageViewsItem] = {
    val tableName = Config().campaignPageviewsTableName
    getResults(Scanamo.query[CampaignPageViewsItem](DynamoClient)(tableName)('campaignId -> campaignId))
  }
}

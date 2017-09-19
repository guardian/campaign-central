package repositories

import cats.implicits._
import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.CampaignPageViewsItem
import model.command.{CampaignCentralApiError, JsonParsingError}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignPageViewsRepository {

  def getCampaignPageViews(campaignId: String): Either[CampaignCentralApiError, List[CampaignPageViewsItem]] = {
    val tableName = Config().campaignPageviewsTableName
    val result    = Scanamo.query[CampaignPageViewsItem](DynamoClient)(tableName)('campaignId -> campaignId)
    getResultsOrFirstFailure(result).leftMap { e =>
      JsonParsingError(e.show)
    }
  }
}

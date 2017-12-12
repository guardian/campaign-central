package repositories

import cats.implicits._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}
import model.{CampaignCentralApiError, CampaignPageViewsItem, JsonParsingError, Territory}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignPageViewsRepository {

  private val table = Table[CampaignPageViewsItem](Config().campaignPageviewsTableName)

  def getCampaignPageViews(campaignId: String,
                           territory: Territory): Either[CampaignCentralApiError, List[CampaignPageViewsItem]] = {
    getResultsOrFirstFailure(
      Scanamo.exec(DynamoClient)(
        table.filter('territory -> territory.databaseKeyValue).query('campaignId -> campaignId))).leftMap { e =>
      JsonParsingError(e.show)
    }
  }
}

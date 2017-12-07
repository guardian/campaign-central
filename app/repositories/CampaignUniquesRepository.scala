package repositories

import cats.implicits._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}
import model.{CampaignCentralApiError, CampaignUniquesItem, JsonParsingError, Territory}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignUniquesRepository {

  private val table = Table[CampaignUniquesItem](Config().campaignUniquesTableName)

  def getCampaignUniques(campaignId: String,
                         territory: Territory): Either[CampaignCentralApiError, List[CampaignUniquesItem]] = {
    getResultsOrFirstFailure(
      Scanamo.exec(DynamoClient)(
        table.filter('territory -> territory.databaseKeyValue).query('campaignId -> campaignId))).leftMap { e =>
      JsonParsingError(e.show)
    }
  }
}

package repositories

import cats.implicits._
import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.CampaignUniquesItem
import model.command.{CampaignCentralApiError, JsonParsingError}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignUniquesRepository {

  def getCampaignUniques(campaignId: String): Either[CampaignCentralApiError, List[CampaignUniquesItem]] = {
    val tableName = Config().campaignUniquesTableName
    val result    = Scanamo.query[CampaignUniquesItem](DynamoClient)(tableName)('campaignId -> campaignId)
    getResultsOrFirstFailure(result).left map { e =>
      JsonParsingError(e.show)
    }
  }
}

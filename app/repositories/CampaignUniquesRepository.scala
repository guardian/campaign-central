package repositories

import cats.implicits._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}
import model.CampaignUniquesItem
import model.command.{CampaignCentralApiError, JsonParsingError}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignUniquesRepository {

  private val table = Table[CampaignUniquesItem](Config().campaignUniquesTableName)

  def getCampaignUniques(campaignId: String): Either[CampaignCentralApiError, List[CampaignUniquesItem]] = {
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId))).leftMap { e =>
      JsonParsingError(e.show)
    }
  }
}

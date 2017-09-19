package repositories

import cats.implicits._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{Scanamo, Table}
import model.CampaignPageViewsItem
import model.command.{CampaignCentralApiError, JsonParsingError}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignPageViewsRepository {

  private val table = Table[CampaignPageViewsItem](Config().campaignPageviewsTableName)

  def getCampaignPageViews(campaignId: String): Either[CampaignCentralApiError, List[CampaignPageViewsItem]] = {
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId))).leftMap { e =>
      JsonParsingError(e.show)
    }
  }
}

package repositories

import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.CampaignUniquesItem
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

object CampaignUniquesRepository {

  private implicit val logger: Logger = Logger(getClass)

  def getCampaignUniques(campaignId: String): Seq[CampaignUniquesItem] = {
    val query = 'campaignId -> campaignId
    getResults(Scanamo.query[CampaignUniquesItem](DynamoClient)(Config().campaignUniquesTableName)(query))
  }
}

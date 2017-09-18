package repositories

import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.CampaignUniquesItem
<<<<<<< HEAD
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

object CampaignUniquesRepository {

  private implicit val logger: Logger = Logger(getClass)

  def getCampaignUniques(campaignId: String): Seq[CampaignUniquesItem] = {
    val query = 'campaignId -> campaignId
    getResults(Scanamo.query[CampaignUniquesItem](DynamoClient)(Config().campaignUniquesTableName)(query))
=======
import model.command.CampaignCentralApiError
import services.Dynamo
import cats.implicits._

import scala.collection.JavaConversions._

object CampaignUniquesRepository {

  def getCampaignUniques(campaignId: String): Either[CampaignCentralApiError, List[CampaignUniquesItem]] = {
    val result: List[Either[CampaignCentralApiError, CampaignUniquesItem]] =
      Dynamo.campaignUniquesTable.query("campaignId", campaignId).map(CampaignUniquesItem.fromItem).toList
    result.sequence
>>>>>>> master
  }
}

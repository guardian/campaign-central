package repositories

import model.CampaignUniquesItem
import model.command.CampaignCentralApiError
import services.Dynamo
import cats.implicits._

import scala.collection.JavaConversions._

object CampaignUniquesRepository {

  def getCampaignUniques(campaignId: String): Either[CampaignCentralApiError, List[CampaignUniquesItem]] = {
    val result: List[Either[CampaignCentralApiError, CampaignUniquesItem]] =
      Dynamo.campaignUniquesTable.query("campaignId", campaignId).map(CampaignUniquesItem.fromItem).toList
    result.sequence
  }

}

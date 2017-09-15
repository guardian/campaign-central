package repositories

import model.CampaignPageViewsItem
import model.command.CampaignCentralApiError
import services.Dynamo
import cats.implicits._

import scala.collection.JavaConversions._

object CampaignPageViewsRepository {

  def getCampaignPageViews(campaignId: String): Either[CampaignCentralApiError, List[CampaignPageViewsItem]] = {
    val result: List[Either[CampaignCentralApiError, CampaignPageViewsItem]] =
      Dynamo.campaignPageviewsTable.query("campaignId", campaignId).map { CampaignPageViewsItem.fromItem }.toList
    result.sequence
  }

}

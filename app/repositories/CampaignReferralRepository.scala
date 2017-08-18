package repositories

import model.CampaignReferral
import services.Dynamo

import scala.collection.JavaConversions._

object CampaignReferralRepository {

  def getCampaignReferrals(campaignId: String): Seq[CampaignReferral] =
    Dynamo.campaignReferralTable.query("campaignId", campaignId).flatMap(CampaignReferral.fromItem).toList
}

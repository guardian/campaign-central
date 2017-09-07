package model

import java.time.LocalDate

import play.api.libs.json._
import repositories.CampaignReferralRepository

case class Component(
  platform: String,
  edition: String,
  path: String,
  containerIndex: Int,
  containerName: String,
  cardIndex: Int,
  cardName: String
)

object Component {
  implicit val componentWrites: Writes[Component] = Json.writes[Component]
}

// A referral to any item in a campaign from an on-platform source in a particular period
case class CampaignReferral(
  component: Component,
  numClicks: Int,
  firstReferral: LocalDate,
  lastReferral: LocalDate
)

object CampaignReferral {

  implicit val referralWrites: Writes[CampaignReferral] = Json.writes[CampaignReferral]

  def forCampaign(campaignId: String): Seq[CampaignReferral] =
    CampaignReferralRepository.getCampaignReferrals(campaignId)
}

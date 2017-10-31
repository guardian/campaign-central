package model

import play.api.libs.json._

case class CampaignReferral(
  sourceDescription: String,
  stats: ReferralStats,
  children: Option[Seq[CampaignReferral]]
)

object CampaignReferral {
  implicit lazy val writes: Writes[CampaignReferral] = Json.writes[CampaignReferral]
}

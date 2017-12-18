package model

case class SocialReferralRow(
  campaignId: String,
  hash: String,
  referralDate: String,
  referringDomain: String,
  territory: Option[String],
  paid: Boolean,
  clickCount: Long,
)

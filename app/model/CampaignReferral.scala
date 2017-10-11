package model

import java.time.LocalDate

import play.api.libs.json.{Json, Writes}

case class Component(
  platform: String,
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
  clickCount: Int,
  impressionCount: Int,
  // Annoyingly react won't recognise CTR if it's a derived property so it has to go in as part of the constructor
  ctr: Double,
  firstReferral: LocalDate,
  lastReferral: LocalDate
)

object CampaignReferral {
  implicit val referralWrites: Writes[CampaignReferral] = Json.writes[CampaignReferral]

  def apply(
    component: Component,
    clickCount: Int,
    impressionCount: Int,
    firstReferral: LocalDate,
    lastReferral: LocalDate
  ): CampaignReferral = CampaignReferral(
    component,
    clickCount,
    impressionCount,
    clickCount / impressionCount.toDouble,
    firstReferral,
    lastReferral
  )
}

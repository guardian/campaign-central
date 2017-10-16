package model

import java.time.LocalDate

import play.api.libs.json.{JsNumber, Json, Writes}

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
  firstReferral: LocalDate,
  lastReferral: LocalDate
) {
  val ctr: Double = clickCount / impressionCount.toDouble
}

object CampaignReferral {
  implicit val referralWrites: Writes[CampaignReferral] = { referral =>
    Json.writes[CampaignReferral].writes(referral) + ("ctr" -> JsNumber(referral.ctr))
  }
}

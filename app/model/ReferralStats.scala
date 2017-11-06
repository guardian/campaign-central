package model

import play.api.libs.json.{JsNumber, Json, Writes}

case class ReferralStats(
  impressionCount: Int,
  clickCount: Int
) {
  val ctr: Double = clickCount / impressionCount.toDouble
}

object ReferralStats {
  implicit val writes: Writes[ReferralStats] = { stats =>
    Json.writes[ReferralStats].writes(stats) + ("ctr" -> JsNumber(stats.ctr))
  }

  def fromRows(rows: Seq[CampaignReferralRow]): ReferralStats =
    ReferralStats(
      impressionCount = rows.map(_.impressionCount).sum.toInt,
      clickCount = rows.map(_.clickCount).sum.toInt
    )
}

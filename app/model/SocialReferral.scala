package model

import play.api.libs.json.{Json, Writes}

case class SocialReferral(
  referringPlatform: SocialPlatform,
  paid: Boolean,
  clickCount: Int
)

object SocialReferral {
  implicit lazy val writes: Writes[SocialReferral] = Json.writes[SocialReferral]

  def fromRows(rows: Seq[SocialReferralRow]): Seq[SocialReferral] =
    rows
      .groupBy(row => (row.referringDomain, row.paid))
      .map {
        case ((referringDomain, paid), currRows) =>
          SocialReferral(
            referringPlatform = SocialPlatform.fromDomain(referringDomain),
            paid = paid,
            clickCount = currRows.map(_.clickCount).sum.toInt
          )
      }
      .toSeq
      .sortBy(-_.clickCount)
}

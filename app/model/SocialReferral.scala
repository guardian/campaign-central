package model

import play.api.libs.json.{Json, Writes}

case class SocialReferral(
  referringPlatform: SocialPlatform,
  organicClickCount: Int,
  paidClickCount: Int
)

object SocialReferral {
  implicit lazy val writes: Writes[SocialReferral] = Json.writes[SocialReferral]

  def fromRows(rows: Seq[SocialReferralRow]): Seq[SocialReferral] =
    rows
      .groupBy(_.referringDomain)
      .map {
        case (referringDomain, currRows) =>
          SocialReferral(
            referringPlatform = SocialPlatform.fromDomain(referringDomain),
            organicClickCount = currRows.filterNot(_.paid).map(_.clickCount).sum.toInt,
            paidClickCount = currRows.filter(_.paid).map(_.clickCount).sum.toInt
          )
      }
      .toSeq
      .sortBy(r => (-r.organicClickCount, -r.paidClickCount))
}

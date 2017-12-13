package model

import org.scalacheck.Prop.forAll
import org.scalacheck._

object SocialReferralFromRowsSpec extends Properties("SocialReferral.fromRows") {

  private implicit lazy val arbRow: Arbitrary[SocialReferralRow] = Arbitrary(
    for {
      referralDate    <- Gen.alphaNumStr
      referringDomain <- Gen.oneOf("facebook.com", "twitter.com")
      clickCount      <- Gen.choose[Long](0, 1000000000)
      paid            <- Gen.oneOf(true, false)
    } yield {
      SocialReferralRow(
        campaignId = "campaignId",
        hash = "hash",
        referralDate = referralDate,
        referringDomain = referringDomain,
        territory = "territory",
        paid = paid,
        clickCount = clickCount
      )
    }
  )

  property("Number of referrals is same as number of referring domains") = forAll { rows: Seq[SocialReferralRow] =>
    val referrals        = SocialReferral.fromRows(rows)
    val referringDomains = rows.map(_.referringDomain).distinct
    referrals.lengthCompare(referringDomains.size) == 0
  }

  property("Clicks for a referral are sum of clicks for corresponding referring domain") = forAll {
    rows: Seq[SocialReferralRow] =>
      val referral        = SocialReferral.fromRows(rows).headOption
      val referringDomain = referral.map(_.referringPlatform.domain)
      def clickCount(p: SocialReferralRow => Boolean) =
        referringDomain
          .map { domain =>
            rows
              .filter(row => row.referringDomain == domain && p(row))
              .map(_.clickCount)
              .sum
              .toInt
          }
          .getOrElse(0)
      val organicClicksFromRows = clickCount(!_.paid)
      val paidClicksFromRows    = clickCount(_.paid)
      referral.forall { r =>
        r.organicClickCount == organicClicksFromRows && r.paidClickCount == paidClicksFromRows
      }
  }
}

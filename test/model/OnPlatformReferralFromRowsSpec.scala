package model

import org.scalacheck.Prop.{BooleanOperators, forAll}
import org.scalacheck._

object OnPlatformReferralFromRowsSpec extends Properties("OnPlatformReferral.fromRows") {

  private implicit lazy val arbRow: Arbitrary[OnPlatformReferralRow] = Arbitrary(
    for {
      referralDate    <- Gen.alphaNumStr
      platform        <- Gen.alphaStr
      path            <- Gen.option(Gen.alphaStr)
      containerIndex  <- Gen.option(Gen.choose(1, 50))
      containerName   <- Gen.option(Gen.alphaStr)
      cardIndex       <- Gen.option(Gen.choose(1, 50))
      cardName        <- Gen.option(Gen.alphaStr)
      clickCount      <- Gen.choose[Long](0, 1000000000)
      impressionCount <- Gen.choose[Long](0, 1000000000)
    } yield {
      OnPlatformReferralRow(
        campaignId = "campaignId",
        hash = "hash",
        referralDate = referralDate,
        platform = platform,
        edition = Some("edition"),
        path = path,
        containerIndex = containerIndex,
        containerName = containerName,
        cardIndex = cardIndex,
        cardName = cardName,
        clickCount = clickCount,
        impressionCount = impressionCount,
        unparsedComponent = Some("unparsedComponent")
      )
    }
  )

  property("Tree root is singular") = forAll { rows: Seq[OnPlatformReferralRow] =>
    rows.forall(_.path.isEmpty) ==> OnPlatformReferral.fromRows(rows).isEmpty
    rows.exists(_.path.nonEmpty) ==> { OnPlatformReferral.fromRows(rows).lengthCompare(1) == 0 }
  }

  property("Tree root contains stats from all rows with non-empty paths") = forAll { rows: Seq[OnPlatformReferralRow] =>
    rows.exists(_.path.nonEmpty) ==> {
      val rowsWithPaths = rows.filter(_.path.nonEmpty)
      OnPlatformReferral.fromRows(rowsWithPaths).head.stats == ReferralStats(
        impressionCount = rowsWithPaths.map(_.impressionCount).sum.toInt,
        clickCount = rowsWithPaths.map(_.clickCount).sum.toInt
      )
    }
  }

  property("Stats at any arbitrary level sum to stats of parent") = forAll { rows: Seq[OnPlatformReferralRow] =>
    val statsMatch = for {
      grandparent <- OnPlatformReferral.fromRows(rows).headOption
      parent      <- grandparent.children.flatMap(_.headOption)
      children    <- parent.children
    } yield {
      val childrenStats = children.map(_.stats)
      parent.stats == ReferralStats(
        impressionCount = childrenStats.map(_.impressionCount).sum,
        clickCount = childrenStats.map(_.clickCount).sum
      )
    }
    statsMatch.forall(result => result)
  }
}

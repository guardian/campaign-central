package model

import play.api.libs.json._

case class CampaignReferral(
  sourceDescription: String,
  stats: ReferralStats,
  children: Option[Seq[CampaignReferral]]
)

object CampaignReferral {
  implicit lazy val writes: Writes[CampaignReferral] = Json.writes[CampaignReferral]

  def fromRows(rows: Seq[CampaignReferralRow]): Seq[CampaignReferral] = {

    def childReferrals[A](rows: Seq[CampaignReferralRow])(group: CampaignReferralRow => A)(description: A => String)(
      children: Seq[CampaignReferralRow] => Option[Seq[CampaignReferral]]): List[CampaignReferral] =
      rows
        .groupBy(group)
        .flatMap {
          case (a, subRows) =>
            Some(
              CampaignReferral(
                sourceDescription = description(a),
                stats = ReferralStats.fromRows(subRows),
                children = children(subRows)
              ))
        }
        .toList
        .sortBy(ref => (-ref.stats.ctr, ref.sourceDescription))

    def formatPlatform(platform: String): String = platform match {
      case "NEXT_GEN"           => "Web"
      case "ANDROID_NATIVE_APP" => "Android"
      case "IOS_NATIVE_APP"     => "iOS"
      case "WINDOWS_NATIVE_APP" => "Windows"
      case other                => other
    }

    val filtered = rows
      .filter { row =>
        row.path.nonEmpty && row.impressionCount > 0
      }

    def referralsByPath(pathRows: Seq[CampaignReferralRow]): Seq[CampaignReferral] =
      childReferrals(pathRows)(_.formattedPath)(identity) { containerRows =>
        Some(referralsByContainer(containerRows))
      }

    def referralsByContainer(containerRows: Seq[CampaignReferralRow]): Seq[CampaignReferral] =
      childReferrals(containerRows)(_.formattedContainerName) {
        case Some(containerName) => s"Container: $containerName"
        case None                => ""
      } { cardRows =>
        Some(referralsByCard(cardRows))
      }

    def referralsByCard(cardRows: Seq[CampaignReferralRow]): Seq[CampaignReferral] =
      childReferrals(cardRows) { row =>
        (row.cardIndex, row.cardName)
      } {
        case (Some(cardIndex), Some(cardName)) => s"Card #$cardIndex: $cardName"
        case _                                 => ""
      } { platformRows =>
        Some(referralsByPlatform(platformRows))
      }

    def referralsByPlatform(platformRows: Seq[CampaignReferralRow]): Seq[CampaignReferral] =
      childReferrals(platformRows)(_.platform)(formatPlatform)(dateRows => Some(referralsByDate(dateRows)))

    def referralsByDate(dateRows: Seq[CampaignReferralRow]): Seq[CampaignReferral] =
      childReferrals(dateRows)(_.referralDate)(identity)(_ => None)

    val treeRoot = childReferrals(filtered)(_ => 1)(_ => "All fronts") { pathRows =>
      Some(referralsByPath(pathRows))
    }

    treeRoot
  }
}

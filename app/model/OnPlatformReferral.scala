package model

import play.api.libs.json._

case class OnPlatformReferral(
  sourceDescription: String,
  stats: ReferralStats,
  children: Option[Seq[OnPlatformReferral]]
)

object OnPlatformReferral {
  implicit lazy val writes: Writes[OnPlatformReferral] = Json.writes[OnPlatformReferral]

  def fromRows(rows: Seq[OnPlatformReferralRow]): Seq[OnPlatformReferral] = {

    def childReferrals[A](rows: Seq[OnPlatformReferralRow])(group: OnPlatformReferralRow => A)(
      description: A => String)(
      children: Seq[OnPlatformReferralRow] => Option[Seq[OnPlatformReferral]]): List[OnPlatformReferral] =
      rows
        .groupBy(group)
        .flatMap {
          case (a, subRows) =>
            Some(
              OnPlatformReferral(
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

    def referralsByPath(pathRows: Seq[OnPlatformReferralRow]): Seq[OnPlatformReferral] =
      childReferrals(pathRows)(_.formattedPath)(identity) { containerRows =>
        Some(referralsByContainer(containerRows))
      }

    def referralsByContainer(containerRows: Seq[OnPlatformReferralRow]): Seq[OnPlatformReferral] =
      childReferrals(containerRows)(_.formattedContainerName) {
        case Some(containerName) => s"Container: $containerName"
        case None                => ""
      } { cardRows =>
        Some(referralsByCard(cardRows))
      }

    def referralsByCard(cardRows: Seq[OnPlatformReferralRow]): Seq[OnPlatformReferral] =
      childReferrals(cardRows) { row =>
        (row.cardIndex, row.cardName)
      } {
        case (Some(cardIndex), Some(cardName)) => s"Card #$cardIndex: $cardName"
        case _                                 => ""
      } { platformRows =>
        Some(referralsByPlatform(platformRows))
      }

    def referralsByPlatform(platformRows: Seq[OnPlatformReferralRow]): Seq[OnPlatformReferral] =
      childReferrals(platformRows)(_.platform)(formatPlatform)(dateRows => Some(referralsByDate(dateRows)))

    def referralsByDate(dateRows: Seq[OnPlatformReferralRow]): Seq[OnPlatformReferral] =
      childReferrals(dateRows)(_.referralDate)(identity)(_ => None)

    val treeRoot = childReferrals(filtered)(_ => 1)(_ => "All fronts") { pathRows =>
      Some(referralsByPath(pathRows))
    }

    treeRoot
  }
}

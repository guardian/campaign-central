package repositories

import cats.implicits._
import com.gu.scanamo._
import com.gu.scanamo.syntax._
import model._
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignReferralRepository {

  private val table = Table[CampaignReferralRow](Config().campaignReferralTableName)

  def getCampaignReferrals(campaignId: String): Either[CampaignCentralApiError, List[CampaignReferral]] = {

    def subReferrals[A](rows: Seq[CampaignReferralRow])(group: CampaignReferralRow => A)(description: A => String)(
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

    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId))) match {

      case Left(e) => Left(JsonParsingError(e.show))

      case Right(rows) =>
        val filtered = rows
          .filter { row =>
            row.path.nonEmpty && row.impressionCount > 0
          }

        val referrals = subReferrals(filtered)(_.formattedPath)(identity) { pathRows =>
          Some(subReferrals(pathRows) { row =>
            (row.containerIndex, row.containerName)
          } {
            case (Some(containerIndex), Some(containerName)) => s"Container #$containerIndex: $containerName"
            case _                                           => ""
          } { containerRows =>
            Some(subReferrals(containerRows) { row =>
              (row.cardIndex, row.cardName)
            } {
              case (Some(cardIndex), Some(cardName)) => s"Card #$cardIndex: $cardName"
              case _                                 => ""
            } { cardRows =>
              Some(subReferrals(cardRows)(_.platform)(formatPlatform) { platformRows =>
                Some(subReferrals(platformRows)(_.referralDate)(identity) { _ =>
                  None
                })
              })
            })
          })
        }

        Right(referrals)
    }
  }

  private def formatPlatform(platform: String): String = platform match {
    case "NEXT_GEN"           => "Web"
    case "ANDROID_NATIVE_APP" => "Android"
    case "IOS_NATIVE_APP"     => "iOS"
    case "WINDOWS_NATIVE_APP" => "Windows"
    case other                => other
  }
}

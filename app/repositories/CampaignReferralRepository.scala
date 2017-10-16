package repositories

import java.time.LocalDate

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
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId))) match {

      case Left(e) => Left(JsonParsingError(e.show))

      case Right(rows) =>
        val groupedRows = rows
          .filter { row =>
            row.path.nonEmpty && row.impressionCount > 0
          }
          .groupBy { row =>
            (row.platform, row.formattedPath, row.containerIndex, row.cardIndex)
          }

        val campaignReferrals = groupedRows
          .map { row =>
            val (platform, path, containerIndex, cardIndex) = row._1
            val groupedValues                               = row._2
            CampaignReferral(
              component = Component(
                platform = formatPlatform(platform),
                path = path,
                containerIndex = containerIndex getOrElse 0,
                containerName = groupedValues.headOption.flatMap(_.containerName) getOrElse "",
                cardIndex = cardIndex getOrElse 0,
                cardName = groupedValues.headOption.flatMap(_.cardName) getOrElse ""
              ),
              clickCount = groupedValues.map(_.clickCount).sum.toInt,
              impressionCount = groupedValues.map(_.impressionCount).sum.toInt,
              firstReferral = LocalDate.parse(groupedValues.map(_.referralDate).min),
              lastReferral = LocalDate.parse(groupedValues.map(_.referralDate).max)
            )
          }
          .toList
          .sortBy(
            ref =>
              (-ref.ctr,
               ref.component.platform,
               ref.component.path,
               ref.component.containerIndex,
               ref.component.cardIndex))

        Right(campaignReferrals)
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

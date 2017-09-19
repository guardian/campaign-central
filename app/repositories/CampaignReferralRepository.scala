package repositories

import java.time.LocalDate

import cats.implicits._
import com.gu.scanamo._
import com.gu.scanamo.syntax._
import model.command.{CampaignCentralApiError, JsonParsingError}
import model.{CampaignReferral, CampaignReferralRow, Component}
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignReferralRepository {

  private val table = Table[CampaignReferralRow](Config().campaignReferralTableName)

  def getCampaignReferrals(campaignId: String): Either[CampaignCentralApiError, List[CampaignReferral]] = {
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId))) match {

      case Left(e) => Left(JsonParsingError(e.show))

      case Right(rows) =>
        val groupedRows = rows.groupBy(row => (row.platform, row.edition, row.path, row.containerIndex, row.cardIndex))

        val campaignReferrals = groupedRows
          .map { row =>
            val (platform, edition, path, containerIndex, cardIndex) = row._1
            val groupedValues                                        = row._2
            CampaignReferral(
              component = Component(
                platform = formatPlatform(platform),
                edition = edition getOrElse "unknown",
                path = path getOrElse "unknown",
                containerIndex = containerIndex getOrElse 0,
                containerName = groupedValues.headOption.flatMap(_.containerName) getOrElse "",
                cardIndex = cardIndex getOrElse 0,
                cardName = groupedValues.headOption.flatMap(_.cardName) getOrElse ""
              ),
              numClicks = groupedValues.map(_.clicks).sum.toInt,
              firstReferral = LocalDate.parse(groupedValues.map(_.referralDate).min),
              lastReferral = LocalDate.parse(groupedValues.map(_.referralDate).max)
            )
          }
          .toList
          .sortBy(_.numClicks)
          .reverse

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

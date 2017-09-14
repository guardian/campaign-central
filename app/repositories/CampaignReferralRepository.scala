package repositories

import java.time.LocalDate

import com.gu.scanamo._
import com.gu.scanamo.syntax._
import model.{CampaignReferral, CampaignReferralRow, Component}
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

object CampaignReferralRepository {

  private implicit val logger: Logger = Logger(getClass)

  def getCampaignReferrals(campaignId: String): Seq[CampaignReferral] = {

    val rows = {
      val query = 'campaignId -> campaignId
      getResults(Scanamo.query[CampaignReferralRow](DynamoClient)(Config().campaignReferralTableName)(query))
    }

    val groupedRows = rows.groupBy(row => (row.platform, row.edition, row.path, row.containerIndex, row.cardIndex))

    def formatPlatform(platform: String): String = platform match {
      case "NEXT_GEN"           => "Web"
      case "ANDROID_NATIVE_APP" => "Android"
      case "IOS_NATIVE_APP"     => "iOS"
      case "WINDOWS_NATIVE_APP" => "Windows"
      case other                => other
    }

    groupedRows
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
  }
}

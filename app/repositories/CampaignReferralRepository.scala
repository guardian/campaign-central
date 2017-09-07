package repositories

import java.time.LocalDate

import com.amazonaws.services.dynamodbv2.document.Item
import model.{CampaignReferral, Component}
import play.api.Logger
import play.api.libs.json._
import services.Dynamo

import scala.collection.JavaConversions._
import scala.util.control.NonFatal

object CampaignReferralRepository {

  def getCampaignReferrals(campaignId: String): Seq[CampaignReferral] = {

    val rows = Dynamo.campaignReferralTable
      .query("campaignId", campaignId)
      .flatMap(CampaignReferralRow.fromItem)
      .toList

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

case class CampaignReferralRow(
  campaignId: String,
  hash: String,
  referralDate: String,
  platform: String,
  edition: Option[String],
  path: Option[String],
  containerIndex: Option[Int],
  containerName: Option[String],
  cardIndex: Option[Int],
  cardName: Option[String],
  clicks: Long,
  unparsedComponent: Option[String]
)

object CampaignReferralRow {

  implicit val itemReads: Reads[CampaignReferralRow] = Json.reads[CampaignReferralRow]

  def fromItem(item: Item): Option[CampaignReferralRow] =
    try {
      Some(Json.parse(item.toJSON).as[CampaignReferralRow])
    } catch {
      case NonFatal(e) =>
        Logger.error(s"failed to load referral ${item.toJSON}", e)
        None
    }
}

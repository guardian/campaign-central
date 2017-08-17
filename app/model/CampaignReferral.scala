package model

import java.time.LocalDate

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json._
import repositories.CampaignReferralRepository

import scala.util.control.NonFatal

// A referral to any item in a campaign from an on-platform source in a particular period
case class CampaignReferral(
  campaignId: String,
  platform: String,
  edition: String,
  path: String,
  numClicks: Int,
  containerIndex: Int,
  containerName: String,
  cardIndex: Int,
  cardName: String,
  firstReferral: LocalDate,
  lastReferral: LocalDate
)

object CampaignReferral {

  // dynamodb item -> CampaignReferral
  implicit val itemReads: Reads[CampaignReferral] = Reads { json =>
    val platformUrlComponent: Seq[String] = (json \ "platformUrlComponent").as[String].split("\\|").toSeq

    def platform(s: String) = s match {
      case "NEXT_GEN" => "Web"
      case _          => "unknown"
    }

    def ifPresent(s: Option[String]): String = s.map(_.trim) getOrElse "unknown"

    def indexIfPresent(s: Option[String], prefix: String): Int = s.map(_.trim.stripPrefix(prefix).toInt) getOrElse -1

    def asDate(js: JsLookupResult): LocalDate = LocalDate.parse(js.as[String])

    JsSuccess(
      CampaignReferral(
        campaignId = (json \ "campaignId").as[String],
        platform = ifPresent(platformUrlComponent.headOption.map(platform)),
        edition = ifPresent(platformUrlComponent.lift(3)),
        path = ifPresent(platformUrlComponent.lift(4)),
        numClicks = (json \ "clicks").as[Int],
        containerIndex = indexIfPresent(platformUrlComponent.lift(5), "container-"),
        containerName = ifPresent(platformUrlComponent.lift(6)),
        cardIndex = indexIfPresent(platformUrlComponent.lift(8), "card-"),
        cardName = ifPresent(platformUrlComponent.lift(9)),
        firstReferral = asDate(json \ "firstReferral"),
        lastReferral = asDate(json \ "lastReferral")
      )
    )
  }

  implicit val referralWrites: Writes[CampaignReferral] = Json.writes[CampaignReferral]

  def forCampaign(campaignId: String): Seq[CampaignReferral] = {
    val (unknown, known) = CampaignReferralRepository.getCampaignReferrals(campaignId) partition (_.path == "unknown")
    unknown foreach { referral =>
      Logger.warn(s"Unknown referral: $referral")
    }
    known
  }

  def fromItem(item: Item): Option[CampaignReferral] =
    try {
      Some(Json.parse(item.toJSON).as[CampaignReferral])
    } catch {
      case NonFatal(e) =>
        Logger.error(s"failed to load referral ${item.toJSON}", e)
        None
    }
}

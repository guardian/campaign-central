package model

import java.math.BigInteger
import java.time.LocalDate

import com.amazonaws.services.dynamodbv2.document.Item
import play.api.Logger
import play.api.libs.json._
import repositories.CampaignReferralRepository

import scala.util.control.NonFatal

case class Component(
  platform: String,
  edition: String,
  path: String,
  containerIndex: Int,
  containerName: String,
  cardIndex: Int,
  cardName: String
) {

  object MD5 {
    def hash(s: String): String = {
      val digest = java.security.MessageDigest.getInstance("MD5")
      val bytes  = s.getBytes("UTF-8")
      digest.update(bytes, 0, bytes.length)
      new BigInteger(1, digest.digest()).toString(16)
    }
  }

  val hash: String = MD5.hash(toString)
}

object Component {
  implicit val componentWrites: Writes[Component] = Json.writes[Component]
}

// A referral to any item in a campaign from an on-platform source in a particular period
case class CampaignReferral(
  campaignId: String,
  component: Component,
  numClicks: Int,
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
        component = Component(
          platform = ifPresent(platformUrlComponent.headOption.map(platform)),
          edition = ifPresent(platformUrlComponent.lift(3)),
          path = ifPresent(platformUrlComponent.lift(4)),
          containerIndex = indexIfPresent(platformUrlComponent.lift(5), "container-"),
          containerName = ifPresent(platformUrlComponent.lift(6)),
          cardIndex = indexIfPresent(platformUrlComponent.lift(8), "card-"),
          cardName = ifPresent(platformUrlComponent.lift(9))
        ),
        numClicks = (json \ "clicks").as[Int],
        firstReferral = asDate(json \ "firstReferral"),
        lastReferral = asDate(json \ "lastReferral")
      )
    )
  }

  implicit val referralWrites: Writes[CampaignReferral] = Json.writes[CampaignReferral]

  def forCampaign(campaignId: String): Seq[CampaignReferral] = {
    val (unknown, known) = CampaignReferralRepository.getCampaignReferrals(campaignId) partition (_.component.path == "unknown")
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

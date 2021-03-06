package model

import cats.syntax.either._
import com.amazonaws.services.dynamodbv2.document.Item
import play.api.libs.json.{Json, Reads}

case class OnPlatformReferralRow(
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
  clickCount: Long,
  impressionCount: Long,
  unparsedComponent: Option[String]
) {
  val formattedPath: String                  = OnPlatformReferralRow.formatPath(path)
  val formattedContainerName: Option[String] = OnPlatformReferralRow.formatContainerName(platform, containerName)
}

object OnPlatformReferralRow {

  implicit val itemReads: Reads[OnPlatformReferralRow] = Json.reads[OnPlatformReferralRow]

  private val editionIds = Set("uk", "us", "au", "international")

  def fromItem(item: Item): Either[CampaignCentralApiError, OnPlatformReferralRow] =
    Either.catchNonFatal(Json.parse(item.toJSON).as[OnPlatformReferralRow]).leftMap(e => JsonParsingError(e.getMessage))

  def formatPath(maybePath: Option[String]): String = {

    def prefixedByEdition(path: String): Boolean = path.contains('/') && editionIds.contains(path.takeWhile(_ != '/'))

    maybePath match {
      case Some(s) if prefixedByEdition(s) => s.dropWhile(_ != '/')
      case Some(f @ "unknown front id")    => f
      case Some("INT")                     => "/international"
      case Some(p)                         => s"/${p.stripPrefix("/").toLowerCase}"
      case _                               => "unknown"
    }
  }

  def formatContainerName(platform: String, containerName: Option[String]): Option[String] = {

    def cleanAndroidContainer(name: Option[String]): Option[String] = name.map(_.replace('-', ' '))

    if (platform == "ANDROID_NATIVE_APP") cleanAndroidContainer(containerName)
    else containerName
  }
}

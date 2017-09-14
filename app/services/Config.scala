package services

import java.io.InputStream
import java.util.Properties

import com.amazonaws.services.s3.model.GetObjectRequest
import services.Config._

import scala.collection.JavaConversions._

object Config extends AwsInstanceTags {

  lazy val conf = readTag("Stage") match {
    case Some("PROD") => new ProdConfig
    case _            => new DevConfig
  }

  def apply() = {
    conf
  }
}

sealed trait Config {

  def stage: String

  def googleAuthClientId     = getRequiredRemoteStringProperty("googleauth.client.id")
  def googleAuthClientSecret = getRequiredRemoteStringProperty("googleauth.client.secret")
  def googleAuthRedirectUrl  = getRequiredRemoteStringProperty("googleauth.redirect.url")
  def googleAuthDomain       = getRequiredRemoteStringProperty("googleauth.domain")

  def logShippingStreamName: Option[String] = None

  def campaignTableName        = s"campaign-central-$stage-campaigns"
  def campaignNotesTableName   = s"campaign-central-$stage-campaign-notes"
  def campaignContentTableName = s"campaign-central-$stage-campaign-content"

  def clientTableName = s"campaign-central-$stage-clients"

  def analyticsDataCacheTableName = s"campaign-central-$stage-analytics"

  def campaignPageviewsTableName       = s"campaign-central-$stage-campaign-page-views"
  def campaignUniquesTableName         = s"campaign-central-$stage-campaign-uniques"
  def latestCampaignAnalyticsTableName = s"campaign-central-$stage-analytics-latest"
  def campaignReferralTableName        = s"campaign-central-$stage-referralsv2"

  def tagManagerApiUrl: String
  def composerUrl: String
  def liveUrl: String
  def previewUrl: String
  def mediaAtomMakerUrl: String
  def ctaAtomMakerUrl: String

  // remote configuration is used for things we don't want to check in to version control
  // such as passwords, private urls, and gossip about other teams

  private lazy val stack              = readTag("Stack") getOrElse "flexible"
  private lazy val app                = readTag("App") getOrElse "campaign-central"
  private lazy val remoteConfigBucket = s"guconf-${stack}"

  private val remoteConfiguration: Map[String, String] = loadRemoteConfiguration

  lazy val googleAnalyticsViewId      = getRequiredRemoteStringProperty("googleAnalytivsViewId")
  lazy val googleAnalyticsGlabsViewId = getRequiredRemoteStringProperty("googleAnalytivsGlabsViewId")

  lazy val capiKey             = getRequiredRemoteStringProperty("capi.key")
  lazy val capiPreviewUrl      = getRequiredRemoteStringProperty("capi.preview.url")
  lazy val capiPreviewUser     = getRequiredRemoteStringProperty("capi.preview.username")
  lazy val capiPreviewPassword = getRequiredRemoteStringProperty("capi.preview.password")

  def googleServiceAccountJsonInputStream: InputStream = {
    val jsonLocation    = getRequiredRemoteStringProperty("googleServiceAccountCredentialsLocation")
    val credentailsJson = AWS.S3Client.getObject(remoteConfigBucket, s"$app/$jsonLocation")
    credentailsJson.getObjectContent
  }

  private def getRequiredRemoteStringProperty(key: String): String = {
    remoteConfiguration.getOrElse(key, {
      throw new IllegalArgumentException(s"Property '$key' not configured")
    })
  }

  private def loadRemoteConfiguration = {

    def loadPropertiesFromS3(propertiesKey: String, props: Properties): Unit = {
      val s3Properties        = AWS.S3Client.getObject(new GetObjectRequest(remoteConfigBucket, propertiesKey))
      val propertyInputStream = s3Properties.getObjectContent
      try {
        props.load(propertyInputStream)
      } finally {
        try { propertyInputStream.close() } catch { case _: Throwable => /*ignore*/ }
      }
    }

    val props = new Properties()

    loadPropertiesFromS3(s"$app/global.properties", props)
    loadPropertiesFromS3(s"$app/$stage.properties", props)

    props.toMap
  }
}

class DevConfig extends Config {
  // override def stage = "DEV"
  override def stage = "PROD"

  override def logShippingStreamName = Some("elk-CODE-KinesisStream-M03ERGK5PVD9")
  override def tagManagerApiUrl      = "https://tagmanager.code.dev-gutools.co.uk"
  override def composerUrl           = "https://composer.local.dev-gutools.co.uk"
  override def liveUrl               = "https://www.theguardian.com"
  override def previewUrl            = "https://viewer.gutools.co.uk/preview"
  override def mediaAtomMakerUrl     = "https://video.local.dev-gutools.co.uk"
  override def ctaAtomMakerUrl       = "https://cta-atom-maker.local.dev-gutools.co.uk"
}

class ProdConfig extends Config {
  override def stage = "PROD"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")
  override def tagManagerApiUrl      = "https://tagmanager.gutools.co.uk"
  override def composerUrl           = "https://composer.gutools.co.uk"
  override def liveUrl               = "https://www.theguardian.com"
  override def previewUrl            = "https://viewer.gutools.co.uk/preview"
  override def mediaAtomMakerUrl     = "https://video.gutools.co.uk"
  override def ctaAtomMakerUrl       = "https://cta-atom-maker.gutools.co.uk"
}

package services

import java.io.InputStream
import java.util.Properties

import com.amazonaws.services.s3.model.GetObjectRequest
import services.Config._

import scala.collection.JavaConverters._

object Config extends AwsInstanceTags {

  lazy val conf: Config = readTag("Stage") match {
    case Some("PROD") => new ProdConfig
    case _            => new DevConfig
  }

  def apply(): Config = {
    conf
  }
}

sealed trait Config {

  def stage: String

  def googleAuthClientId: String     = getRequiredRemoteStringProperty("googleauth.client.id")
  def googleAuthClientSecret: String = getRequiredRemoteStringProperty("googleauth.client.secret")
  def googleAuthRedirectUrl: String  = getRequiredRemoteStringProperty("googleauth.redirect.url")
  def googleAuthDomain: String       = getRequiredRemoteStringProperty("googleauth.domain")

  def campaignCentralApiKey: String = getRequiredRemoteStringProperty("campaign-central.api.key")

  def logShippingStreamName: Option[String] = None

  def campaignTableName            = s"campaign-central-$stage-campaigns"
  def campaignContentTableName     = s"campaign-central-$stage-campaign-content"
  def campaignMediaEventsTableName = s"campaign-central-$stage-media"
  def campaignPageviewsTableName       = s"campaign-central-$stage-campaign-page-views"
  def campaignUniquesTableName         = s"campaign-central-$stage-campaign-uniques"
  def latestCampaignAnalyticsTableName = s"campaign-central-$stage-analytics-latest"
  def campaignReferralTableName        = s"campaign-central-$stage-referrals"

  def tagManagerApiUrl: String
  def composerUrl: String
  def liveUrl: String
  def previewUrl: String

  def gaTrackingCode: String

  // remote configuration is used for things we don't want to check in to version control
  // such as passwords, private urls, and gossip about other teams

  private lazy val stack              = readTag("Stack") getOrElse "flexible"
  private lazy val app                = readTag("App") getOrElse "campaign-central"
  private lazy val remoteConfigBucket = s"guconf-${stack}"

  private val remoteConfiguration: Map[String, String] = loadRemoteConfiguration

  lazy val googleAnalyticsViewId: String      = getRequiredRemoteStringProperty("googleAnalytivsViewId")
  lazy val googleAnalyticsGlabsViewId: String = getRequiredRemoteStringProperty("googleAnalytivsGlabsViewId")

  lazy val capiKey: String             = getRequiredRemoteStringProperty("capi.key")
  lazy val capiPreviewUrl: String      = getRequiredRemoteStringProperty("capi.preview.url")
  lazy val capiPreviewUser: String     = getRequiredRemoteStringProperty("capi.preview.username")
  lazy val capiPreviewPassword: String = getRequiredRemoteStringProperty("capi.preview.password")

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

    props.asScala.toMap
  }
}

class DevConfig extends Config {
  override def stage = "DEV"

  override def logShippingStreamName = Some("elk-CODE-KinesisStream-M03ERGK5PVD9")
  override def tagManagerApiUrl      = "https://tagmanager.code.dev-gutools.co.uk"
  override def composerUrl           = "https://composer.local.dev-gutools.co.uk"
  override def liveUrl               = "https://www.theguardian.com"
  override def previewUrl            = "https://viewer.gutools.co.uk/preview"

  override def gaTrackingCode: String = "dummy"
}

class ProdConfig extends Config {
  override def stage = "PROD"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")
  override def tagManagerApiUrl      = "https://tagmanager.gutools.co.uk"
  override def composerUrl           = "https://composer.gutools.co.uk"
  override def liveUrl               = "https://www.theguardian.com"
  override def previewUrl            = "https://viewer.gutools.co.uk/preview"

  override def gaTrackingCode: String = "UA-78705427-12"
}

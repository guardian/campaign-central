package services

import java.io.InputStream
import java.util.Properties

import com.amazonaws.services.s3.model.GetObjectRequest
import services.Config._

import scala.collection.JavaConversions._

object Config extends AwsInstanceTags {

  lazy val conf = readTag("Stage") match {
    case Some("PROD") =>    new ProdConfig
    case Some("CODE") =>    new CodeConfig
    case _ =>               new DevConfig
  }

  def apply() = {
    conf
  }
}

sealed trait Config {
  def stage: String

  def pandaDomain: String
  def pandaAuthCallback: String

  def logShippingStreamName: Option[String] = None

  def campaignTableName = s"campaign-central-$stage-campaigns"
  def campaignNotesTableName = s"campaign-central-$stage-campaign-notes"
  def campaignContentTableName = s"campaign-central-$stage-campaign-content"

  def clientTableName = s"campaign-central-$stage-clients"

  def analyticsDataCacheTableName = s"campaign-central-$stage-analytics"

  def tagManagerApiUrl: String
  def composerUrl: String
  def liveUrl: String
  def previewUrl: String
  def mediaAtomMakerUrl: String
  def ctaAtomMakerUrl: String


  // remote configuration is used for things we don't want to check in to version control
  // such as passwords, private urls, and gossip about other teams


  private lazy val stack = readTag("Stack") getOrElse "flexible"
  private lazy val app = readTag("App") getOrElse "campaign-central"
  private lazy val remoteConfigBucket = s"guconf-${stack}"

  private val remoteConfiguration: Map[String, String] = loadRemoteConfiguration

  lazy val googleAnalyticsViewId = getRequiredRemoteStringProperty("googleAnalytivsViewId")

  lazy val capiKey = getRequiredRemoteStringProperty("capi.key")
  lazy val capiPreviewUrl = getRequiredRemoteStringProperty("capi.preview.url")
  lazy val capiPreviewUser = getRequiredRemoteStringProperty("capi.preview.username")
  lazy val capiPreviewPassword = getRequiredRemoteStringProperty("capi.preview.password")

  val dfpAppName = "Campaign Central"
  lazy val dfpClientId = getRequiredRemoteStringProperty("dfp.client.id")
  lazy val dfpClientSecret = getRequiredRemoteStringProperty("dfp.client.secret")
  lazy val dfpRefreshToken = getRequiredRemoteStringProperty("dfp.refresh.token")
  def dfpNetworkCode: String
  def dfpNativeCardOrderIds: Map[String, Seq[Long]]
  def dfpMerchandisingOrderIds: Map[String, Seq[Long]]
  def dfpCampaignFieldId: Long

  def googleServiceAccountJsonInputStream: InputStream = {
    val jsonLocation = getRequiredRemoteStringProperty("googleServiceAccountCredentialsLocation")
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
      val s3Properties = AWS.S3Client.getObject(new GetObjectRequest(remoteConfigBucket, propertiesKey))
      val propertyInputStream = s3Properties.getObjectContent
      try {
        props.load(propertyInputStream)
      } finally {
        try {propertyInputStream.close()} catch {case _: Throwable => /*ignore*/}
      }
    }

    val props = new Properties()

    loadPropertiesFromS3(s"$app/global.properties", props)
    loadPropertiesFromS3(s"$app/$stage.properties", props)

    props.toMap
  }
}

object StagingDfpProperties {
  val dfpNetworkCode = "158186692"
  val dfpNativeCardOrderIds = Map(
    "hosted" -> Seq(550773372L),
    "paidContent" -> Seq(550773372L)
  )
  val dfpMerchandisingOrderIds = Map(
    "hosted" -> Seq(550774092L),
    "paidContent" -> Seq(550774092L)
  )
  val dfpCampaignFieldId: Long = 26412
}

class DevConfig extends Config {
  override def stage = "DEV"

  override def logShippingStreamName = Some("elk-CODE-KinesisStream-M03ERGK5PVD9")

  override def pandaDomain: String = "local.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.local.dev-gutools.co.uk/oauthCallback"

  override def tagManagerApiUrl = "https://tagmanager.code.dev-gutools.co.uk"
  override def composerUrl = "https://composer.local.dev-gutools.co.uk"
  override def liveUrl = "https://www.theguardian.com"
  override def previewUrl = "https://viewer.gutools.co.uk/preview"
  override def mediaAtomMakerUrl = "https://media-atom-maker.local.dev-gutools.co.uk"
  override def ctaAtomMakerUrl = "https://cta-atom-maker.local.dev-gutools.co.uk"

  override val dfpNetworkCode = StagingDfpProperties.dfpNetworkCode
  override val dfpNativeCardOrderIds = StagingDfpProperties.dfpNativeCardOrderIds
  override val dfpMerchandisingOrderIds = StagingDfpProperties.dfpMerchandisingOrderIds
  override val dfpCampaignFieldId = StagingDfpProperties.dfpCampaignFieldId
}

class CodeConfig extends Config {
  override def stage = "CODE"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")

  override def pandaDomain: String = "code.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.code.dev-gutools.co.uk/oauthCallback"

  override def tagManagerApiUrl = "https://tagmanager.code.dev-gutools.co.uk"
  override def composerUrl = "https://composer.code.dev-gutools.co.uk"
  override def liveUrl = "http://m.code.dev-theguardian.com"
  override def previewUrl = "https://viewer.code.dev-gutools.co.uk/preview"
  override def mediaAtomMakerUrl = "https://media-atom-maker.code.dev-gutools.co.uk"
  override def ctaAtomMakerUrl = "https://cta-atom-maker.code.dev-gutools.co.uk"

  override val dfpNetworkCode = StagingDfpProperties.dfpNetworkCode
  override val dfpNativeCardOrderIds = StagingDfpProperties.dfpNativeCardOrderIds
  override val dfpMerchandisingOrderIds = StagingDfpProperties.dfpMerchandisingOrderIds
  override val dfpCampaignFieldId = StagingDfpProperties.dfpCampaignFieldId
}

class ProdConfig extends Config {
  override def stage = "PROD"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")

  override def pandaDomain: String = "gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.gutools.co.uk/oauthCallback"

  override def tagManagerApiUrl = "https://tagmanager.gutools.co.uk"
  override def composerUrl = "https://composer.gutools.co.uk"
  override def liveUrl = "https://www.theguardian.com"
  override def previewUrl = "https://viewer.gutools.co.uk/preview"
  override def mediaAtomMakerUrl = "https://media-atom-maker.gutools.co.uk"
  override def ctaAtomMakerUrl = "https://cta-atom-maker.gutools.co.uk"

  override val dfpNetworkCode = "59666047"
  override val dfpNativeCardOrderIds = {
    val hosted = 353494647L
    val paid = 347621127L
    Map(
      "hosted" -> Seq(hosted),
      "paidContent" -> Seq(paid)
    )
  }
  val dfpMerchandisingOrderIds = {
    val hosted = 345535767L
    val paid = 211298847L
    val paidUs = 211064247L
    val paidAu = 211090047L
    Map(
      "hosted" -> Seq(hosted),
      "paidContent" -> Seq(paid, paidUs, paidAu)
    )
  }
  override val dfpCampaignFieldId: Long = 9927
}

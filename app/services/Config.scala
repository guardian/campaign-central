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

  def tagManagerApiUrl: String


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

class DevConfig extends Config {
  override def stage = "DEV"

  override def logShippingStreamName = Some("elk-CODE-KinesisStream-M03ERGK5PVD9")

  override def pandaDomain: String = "local.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.local.dev-gutools.co.uk/oauthCallback"

  override def tagManagerApiUrl = "https://tagmanager.local.dev-gutools.co.uk/hyper"
}

class CodeConfig extends Config {
  override def stage = "CODE"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")

  override def pandaDomain: String = "code.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.code.dev-gutools.co.uk/oauthCallback"

  override def tagManagerApiUrl = "https://tagmanager.code.dev-gutools.co.uk/hyper"
}

class ProdConfig extends Config {
  override def stage = "PROD"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")

  override def pandaDomain: String = "gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.gutools.co.uk/oauthCallback"

  override def tagManagerApiUrl = "https://tagmanager.gutools.co.uk/hyper"
}
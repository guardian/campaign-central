package services

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
}

class DevConfig extends Config {
  override def stage = "DEV"

  override def logShippingStreamName = Some("elk-CODE-KinesisStream-M03ERGK5PVD9")

  override def pandaDomain: String = "local.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.local.dev-gutools.co.uk/oauthCallback"
}

class CodeConfig extends Config {
  override def stage = "CODE"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")

  override def pandaDomain: String = "code.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.code.dev-gutools.co.uk/oauthCallback"
}

class ProdConfig extends Config {
  override def stage = "PROD"

  override def logShippingStreamName = Some("elk-PROD-KinesisStream-1PYU4KS1UEQA")

  override def pandaDomain: String = "gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.gutools.co.uk/oauthCallback"
}
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

  def campaignTableName = s"campaign-content-$stage-campaigns"
  def campaignNotesTableName = s"campaign-content-$stage-campaign-notes"
  def campaignContentTableName = s"campaign-content-$stage-campaign-content"
}

class DevConfig extends Config {
  override def stage = "DEV"

  override def pandaDomain: String = "local.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.local.dev-gutools.co.uk/oauthCallback"
}

class CodeConfig extends Config {
  override def stage = "CODE"

  override def pandaDomain: String = "code.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.code.dev-gutools.co.uk/oauthCallback"
}

class ProdConfig extends Config {
  override def stage = "PROD"

  override def pandaDomain: String = "gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.gutools.co.uk/oauthCallback"
}
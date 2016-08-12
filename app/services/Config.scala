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
  def pandaDomain: String
  def pandaAuthCallback: String
}

class DevConfig extends Config {
  override def pandaDomain: String = "local.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.local.dev-gutools.co.uk/oauthCallback"
}

class CodeConfig extends Config {
  override def pandaDomain: String = "code.dev-gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.code.dev-gutools.co.uk/oauthCallback"
}

class ProdConfig extends Config {
  override def pandaDomain: String = "gutools.co.uk"
  override def pandaAuthCallback: String = "https://campaign-central.gutools.co.uk/oauthCallback"
}
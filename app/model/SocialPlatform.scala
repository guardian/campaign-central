package model

import play.api.libs.json.{JsString, Writes}

sealed trait SocialPlatform {
  def domain: String
  def name: String
}

case object Facebook extends SocialPlatform {
  val domain: String = "facebook.com"
  val name: String   = "Facebook"
}

object SocialPlatform {
  implicit lazy val writes: Writes[SocialPlatform] = platform => JsString(platform.name)

  def fromDomain(domain: String): SocialPlatform = {
    domain match {
      case Facebook.domain => Facebook
    }
  }
}

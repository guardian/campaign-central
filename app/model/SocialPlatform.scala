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

case object Instagram extends SocialPlatform {
  val domain: String = "instagram.com"
  val name: String   = "Instagram"
}

case object LinkedIn extends SocialPlatform {
  val domain: String = "linkedin.com"
  val name: String   = "Linked In"
}

case object Pocket extends SocialPlatform {
  val domain: String = "getpocket.com"
  val name: String   = "Pocket"
}

case object Reddit extends SocialPlatform {
  val domain: String = "reddit.com"
  val name: String   = "Reddit"
}

case object Twitter extends SocialPlatform {
  val domain: String = "twitter.com"
  val name: String   = "Twitter"
}

case object Youtube extends SocialPlatform {
  val domain: String = "youtube.com"
  val name: String   = "Youtube"
}

case object Unknown extends SocialPlatform {
  val domain: String = ""
  val name: String   = "Unknown"
}

object SocialPlatform {
  implicit lazy val writes: Writes[SocialPlatform] = platform => JsString(platform.name)

  def fromDomain(domain: String): SocialPlatform = domain match {
    case Facebook.domain  => Facebook
    case Instagram.domain => Instagram
    case LinkedIn.domain  => LinkedIn
    case Pocket.domain    => Pocket
    case Reddit.domain    => Reddit
    case Twitter.domain   => Twitter
    case Youtube.domain   => Youtube
    case _                => Unknown
  }
}

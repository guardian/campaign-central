package controllers

import com.amazonaws.auth.AWSCredentialsProvider
import com.gu.pandahmac.HMACAuthActions
import com.gu.pandomainauth.PanDomain
import com.gu.pandomainauth.action.AuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import play.api.Logger
import services.{AWS, Config}

trait PandaAuthActions extends AuthActions {

  def config: Config

  def aws: AWS

  override lazy val domain: String = config.pandaDomain

  override def authCallbackUrl: String = config.pandaAuthCallback

  override lazy val system: String = "campaignCentral"

  override def cacheValidation = true

  override lazy val awsCredentialsProvider: AWSCredentialsProvider = aws.credentialsProvider

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    Logger.info(s"validating user $authedUser")
    PanDomain.guardianValidation(authedUser)
  }

}

trait HMACPandaAuthActions extends PandaAuthActions with HMACAuthActions {

  override def secret: String = config.pandaHMACSecret
}

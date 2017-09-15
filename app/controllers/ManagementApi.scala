package controllers

import java.util.concurrent.Executors

import com.gu.googleauth.AuthAction
import model.User
import model.command.{Commands, JsonParsingError}
import play.api.Logger
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import repositories.CampaignRepository
import cats.syntax.either._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class ManagementApi(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def refreshExpiringCampaigns = authAction {

    implicit val user: User = User("campaign", "refresher", "labs.beta@guardian.co.uk")

    val expiringCampaigns = CampaignRepository.getAllCampaigns().map { campaigns =>
      campaigns.filter { c =>
        c.status == "live" && c.endDate.exists(_.isBeforeNow)
      }
    }

    expiringCampaigns match {
      case Left(error: JsonParsingError) => BadRequest(error.message)
      case Left(_)                       => InternalServerError
      case Right(campaigns) =>
        campaigns foreach { c =>
          Logger.info(s"campaign ${c.name} is due to expire, refreshing from CAPI")
          Commands.refreshCampaignById(c.id)(user)
        }
        NoContent
    }

  }
}

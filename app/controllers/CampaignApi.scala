package controllers

import com.gu.googleauth.AuthAction
import model._
import play.api.Logger
import play.api.mvc._
import repositories._
import services.CampaignService
import cats.syntax.either._
import model.command._
import play.api.libs.json.Json

class CampaignApi(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  def getCampaign(id: String) = authAction {
    CampaignRepository.getCampaign(id) match {
      case Left(CampaignNotFound(error)) => NotFound(error)
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(campaign)               => Ok(Json.toJson(campaign))
    }
  }

  def getAllCampaigns() = authAction {
    CampaignRepository.getAllCampaigns() match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(campaigns)              => Ok(Json.toJson(campaigns))
    }
  }

  def getLatestCampaignAnalytics() = authAction {
    CampaignService.getLatestCampaignAnalytics() match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(analytics)              => Ok(Json.toJson(analytics))
    }
  }

  def getLatestAnalyticsForCampaign(campaignId: String) = authAction {
    CampaignService.getLatestAnalyticsForCampaign(campaignId) match {
      case Left(LatestCampaignAnalyticsItemNotFound(error)) => NotFound(error)
      case Left(CampaignNotFound(error))                    => NotFound(error)
      case Left(JsonParsingError(error))                    => InternalServerError(error)
      case Left(_)                                          => InternalServerError
      case Right(analytics)                                 => Ok(Json.toJson(analytics))
    }
  }

  def updateCampaign(id: String) = authAction { req =>
    req.body.asJson.flatMap(_.asOpt[Campaign]) match {
      case None => BadRequest("Could not convert json to campaign")
      case Some(campaign) =>
        CampaignRepository.putCampaign(campaign) match {
          case Left(CampaignPutError(_, e)) => InternalServerError(e.getMessage)
          case Left(_)                      => InternalServerError
          case Right(_)                     => Ok(Json.toJson(campaign))
        }
    }
  }

  def deleteCampaign(id: String) = authAction { _ =>
    val result = for {
      _ <- CampaignContentRepository.deleteContentForCampaign(id)
      _ <- CampaignRepository.deleteCampaign(id)
    } yield NoContent

    result getOrElse InternalServerError
  }

  def getCampaignPageViewsFromDatalake(id: String) = authAction { _ =>
    CampaignService.getPageViews(id) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(pageViews)              => Ok(Json.toJson(pageViews))
    }
  }

  // TODO - fix return types
  def getCampaignUniquesFromDatalake(id: String) = authAction { _ =>
    CampaignService.getUniquesDataForGraph(id).map(uniquesData => Ok(Json.toJson(uniquesData))) getOrElse NotFound
  }

  def getCampaignContent(id: String) = authAction { _ =>
    CampaignContentRepository.getContentForCampaign(id) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(content)                => Ok(Json.toJson(content))
    }
  }

  def importFromTag() = authAction { req =>
    implicit val user: User = User(req.user)
    req.body.asJson.flatMap(_.asOpt[ImportCampaignCommand]) map { importCommand =>
      Commands.importCampaign(importCommand) match {
        case Left(_) =>
          InternalServerError
        case Right(campaign) =>
          Ok(Json.toJson(campaign))
      }
    } getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def refreshCampaignFromCAPI(campaignId: String) = authAction { req =>
    implicit val user: User = User(req.user)
    Commands.refreshCampaignById(campaignId) match {
      case Right(campaign)                 => Ok(Json.toJson(campaign))
      case Left(CampaignNotFound(message)) => NotFound(message)
      case Left(_)                         => InternalServerError
    }
  }

  def getCampaignReferrals(campaignId: String) = authAction { _ =>
    CampaignReferralRepository.getCampaignReferrals(campaignId) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(referrals)              => Ok(Json.toJson(referrals))
    }
  }
}

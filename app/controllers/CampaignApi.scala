package controllers

import com.gu.googleauth.AuthAction
import model._
import model.command._
import model.reports._
import play.api.Logger
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.mvc._
import repositories._
import services.CampaignService

class CampaignApi(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  def getCampaign(id: String) = authAction {
    CampaignRepository.getCampaign(id) map { c =>
      Ok(Json.toJson(c))
    } getOrElse NotFound
  }

  def getAllCampaigns() = authAction {
    Ok(Json.toJson(CampaignRepository.getAllCampaigns()))
  }

  def getLatestCampaignAnalytics() = authAction {
    Ok(Json.toJson(CampaignService.getLatestCampaignAnalytics()))
  }

  def getLatestAnalyticsForCampaign(campaignId: String) = authAction {
    Ok(Json.toJson(CampaignService.getLatestAnalyticsForCampaign(campaignId)))
  }

  def updateCampaign(id: String) = authAction { req =>
    req.body.asJson.flatMap(_.asOpt[Campaign]) match {
      case None => BadRequest("Could not convert json to campaign")
      case Some(campaign) =>
        CampaignRepository.putCampaign(campaign)
        Ok(Json.toJson(campaign))
    }
  }

  def deleteCampaign(id: String) = authAction { _ =>
    CampaignContentRepository.deleteContentForCampaign(id)
    CampaignRepository.deleteCampaign(id)
    NoContent
  }

  def getCampaignPageViews(id: String) = authAction { _ =>
    CampaignPageViewsReport.getCampaignPageViewsReport(id).map { c =>
      Ok(Json.toJson(c))
    } getOrElse NotFound
  }

  def getCampaignPageViewsFromDatalake(id: String) = authAction { _ =>
    val pageViews = CampaignService.getPageViews(id)
    Ok(Json.toJson(pageViews))
  }

  def getCampaignUniquesFromDatalake(id: String) = authAction { _ =>
    CampaignService.getUniquesDataForGraph(id).map(uniquesData => Ok(Json.toJson(uniquesData))) getOrElse NotFound
  }

  def getCampaignDailyUniqueUsers(id: String) = authAction { _ =>
    DailyUniqueUsersReport.getDailyUniqueUsersReport(id).map { c =>
      Ok(Json.toJson(c))
    } getOrElse NotFound
  }

  def getCampaignQualifiedPercentagesReport(id: String) = authAction { _ =>
    QualifiedPercentagesReport.getQualifiedPercentagesReportForCampaign(id).map { c =>
      Ok(Json.toJson(c))
    } getOrElse NotFound
  }

  def getCampaignTargetsReport(id: String) = authAction { _ =>
    Ok(
      Json.toJson(
        CampaignTargetsReport.getCampaignTargetsReport(id).getOrElse(CampaignTargetsReport(Map()))
      ))
  }

  def getCampaignContent(id: String) = authAction { _ =>
    Ok(Json.toJson(CampaignContentRepository.getContentForCampaign(id)))
  }

  def importFromTag() = authAction { req =>
    implicit val user: User = User(req.user)
    req.body.asJson map { json =>
      val importCommand: ImportCampaignCommand = json.as[ImportCampaignCommand]
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

  def getCampaignCtaStats(campaignId: String) = authAction { _ =>
    Ok(toJson(CtaClicksReport.getCtaClicksForCampaign(campaignId)))
  }

  def getCampaignReferrals(campaignId: String) = authAction { _ =>
    Logger.info(s"Loading on-platform referrals for campaign $campaignId")
    Ok(toJson(CampaignReferral.forCampaign(campaignId)))
  }
}

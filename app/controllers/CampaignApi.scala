package controllers

import com.gu.googleauth.AuthAction
import model._
import model.command.CommandError._
import model.command.{ImportCampaignFromCAPICommand, RefreshCampaignFromCAPICommand}
import model.reports._
import org.joda.time.DateTime
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
    CampaignNotesRepository.deleteNotesForCampaign(id)
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

  def getCampaignNotes(id: String) = authAction { _ =>
    Ok(Json.toJson(CampaignNotesRepository.getNotesForCampaign(id)))
  }

  def addCampaignNote(id: String) = authAction { req =>
    val content = (req.body.asJson.get \ "content").as[String]

    if (content.isEmpty)
      BadRequest("Cannot add a note with no content")
    else {
      val created        = DateTime.now()
      val lastModified   = created
      val createdBy      = User(req.user)
      val lastModifiedBy = createdBy

      val newNote = Note(
        campaignId = id,
        created = created,
        createdBy = createdBy,
        lastModified = lastModified,
        lastModifiedBy = lastModifiedBy,
        content = content
      )

      CampaignNotesRepository.putNote(newNote)
      Ok(Json.toJson(newNote))
    }
  }

  def updateCampaignNote(id: String, date: String): Action[AnyContent] = {

    authAction { req =>
      val dateCreated = new DateTime(date.toLong)

      CampaignNotesRepository.getNote(id, dateCreated) match {
        case None => NotFound
        case Some(note) =>
          val lastModified = DateTime.now()
          val modifiedBy   = User(req.user)
          val content      = (req.body.asJson.get \ "content").as[String]

          val updatedNote = note.copy(
            lastModified = lastModified,
            lastModifiedBy = modifiedBy,
            content = content
          )

          CampaignNotesRepository.putNote(updatedNote)
          Ok(Json.toJson(updatedNote))
      }
    }
  }

  def importFromTag() = authAction { req =>
    implicit val user: Option[User] = Option(User(req.user))
    req.body.asJson map { json =>
      json.as[ImportCampaignFromCAPICommand].process() match {
        case Left(e) =>
          commandErrorAsResult(e)
        case Right(campaign) =>
          campaign map (t => Ok(Json.toJson(t))) getOrElse NotFound
      }
    } getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def refreshCampaignFromCAPI(campaignId: String) = authAction { req =>
    implicit val user: Option[User] = Option(User(req.user))
    RefreshCampaignFromCAPICommand(campaignId).process() match {
      case Left(e) =>
        commandErrorAsResult(e)
      case Right(campaign) =>
        campaign map (t => Ok(Json.toJson(t))) getOrElse NotFound
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

package controllers

import model._
import model.command.CommandError._
import model.command.{ImportCampaignFromCAPICommand, RefreshCampaignFromCAPICommand}
import model.reports._
import org.joda.time.{DateTime, _}
import play.api.Logger
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import repositories._
import services.CampaignService

class CampaignApi(override val wsClient: WSClient, components: ControllerComponents)
  extends CentralController(components) with PandaAuthActions {

  def getAllCampaigns() = APIAuthAction {
    Ok(Json.toJson(CampaignRepository.getAllCampaigns()))
  }

  def getAnalyticsSummary() = APIAuthAction {
    Ok(Json.toJson(OverallSummaryReport.getOverallSummaryReport().getOrElse(OverallSummaryReport(Map()))))
  }

  def getCampaign(id: String) = APIAuthAction {
    CampaignRepository.getCampaign(id) map { c => Ok(Json.toJson(c))} getOrElse NotFound
  }

  def updateCampaign(id: String) = APIAuthAction { req =>
    req.body.asJson.flatMap(_.asOpt[Campaign]) match {
      case None => BadRequest("Could not convert json to campaign")
      case Some(campaign) =>
        CampaignRepository.putCampaign(campaign)
        Ok(Json.toJson(campaign))
    }
  }

  def deleteCampaign(id: String) = APIAuthAction { req =>
    CampaignNotesRepository.deleteNotesForCampaign(id)
    CampaignContentRepository.deleteContentForCampaign(id)
    CampaignRepository.deleteCampaign(id)
    NoContent
  }

  def getCampaignPageViews(id: String) = APIAuthAction { req =>
    CampaignPageViewsReport.getCampaignPageViewsReport(id).map { c => Ok(Json.toJson(c)) } getOrElse NotFound
  }

  def getCampaignPageViewsFromDatalake(id: String) = APIAuthAction { req =>
    val pageViews = CampaignService.getPageViews(id)
    Ok(Json.toJson(pageViews))
  }


  def getCampaignUniquesFromDatalake(id: String) = APIAuthAction { req =>
    val campaignUniques = CampaignService.getUniques(id)
    val initialDataPoint = campaignUniques.headOption.map { item =>
      item.copy(reportExecutionTimestamp = new DateTime(item.reportExecutionTimestamp).minusDays(1).toString, uniques = 0L)
    }

    val uniqueItems = initialDataPoint ++ campaignUniques
    val target = CampaignRepository.getCampaign(id).flatMap(_.targets.get("uniques"))

    target match {
      case Some(t) =>
        val runRateStep = {
          val numItems = uniqueItems.size.toLong
          if (numItems == 0) 1
          else t / numItems
        }
        val runRate = Seq.range[Long](0, t + runRateStep, runRateStep)
        val dataPoints = (uniqueItems zip runRate).map { case (unique, rate) =>
          GraphDataPoint(
            name = unique.reportExecutionTimestamp,
            dataPoint = unique.uniques,
            target = rate
          )
        }

        Ok(Json.toJson(dataPoints))

      case None => NotFound
    }

  }

  def getCampaignDailyUniqueUsers(id: String) = APIAuthAction { req =>
    DailyUniqueUsersReport.getDailyUniqueUsersReport(id).map { c => Ok(Json.toJson(c)) } getOrElse NotFound
  }

  def getCampaignQualifiedPercentagesReport(id: String) = APIAuthAction { req =>
    QualifiedPercentagesReport.getQualifiedPercentagesReportForCampaign(id).map { c => Ok(Json.toJson(c)) } getOrElse NotFound
  }

  def getCampaignTargetsReport(id: String) = APIAuthAction { req =>
    Ok(Json.toJson(
      CampaignTargetsReport.getCampaignTargetsReport(id).getOrElse(CampaignTargetsReport(Map()))
    ))
  }

  def getCampaignContent(id: String) = APIAuthAction { req =>
    Ok(Json.toJson(CampaignContentRepository.getContentForCampaign(id)))
  }

  def getCampaignNotes(id: String) = APIAuthAction { req =>
    Ok(Json.toJson(CampaignNotesRepository.getNotesForCampaign(id)))
  }

  def addCampaignNote(id: String) = APIAuthAction { req =>

    val content = (req.body.asJson.get \ "content").as[String]

    if (content.isEmpty)
      BadRequest("Cannot add a note with no content")
    else {
      val created = DateTime.now()
      val lastModified = created
      val createdBy = User(req.user)
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

  def updateCampaignNote(id: String, date: String) = {

    APIAuthAction { req =>

      val dateCreated = new DateTime(date.toLong)

      CampaignNotesRepository.getNote(id, dateCreated) match {
        case None => NotFound
        case Some(note) =>

          val lastModified = DateTime.now()
          val modifiedBy = User(req.user)
          val content = (req.body.asJson.get \ "content").as[String]

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

  def importFromTag() = APIAuthAction { req =>
    implicit val user = Option(User(req.user))
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

  def refreshCampaignFromCAPI(campaignId: String) = APIAuthAction { req =>
    implicit val user = Option(User(req.user))
    RefreshCampaignFromCAPICommand(campaignId).process() match {
      case Left(e) =>
        commandErrorAsResult(e)
      case Right(campaign) =>
        campaign map (t => Ok(Json.toJson(t))) getOrElse NotFound
    }
  }


  def getCampaignTrafficDrivers(campaignId: String) = APIAuthAction { req =>
    Logger.info(s"Loading traffic drivers for campaign $campaignId")
    Ok(toJson(TrafficDriverGroup.forCampaign(campaignId)))
  }

  def getSuggestedCampaignTrafficDrivers(campaignId: String) = APIAuthAction { req =>
    Logger.info(s"Loading suggested traffic drivers for campaign $campaignId")
    Ok(toJson(LineItemSummary.suggestedTrafficDriversForCampaign(campaignId)))
  }

  def acceptSuggestedCampaignTrafficDriver(campaignId: String, lineItemId: Long) = APIAuthAction { req =>
    Logger.info(s"Accepting traffic driver $lineItemId for campaign $campaignId")
    LineItemSummary.acceptSuggestedTrafficDriver(campaignId, lineItemId)
    NoContent
  }

  def rejectSuggestedCampaignTrafficDriver(campaignId: String, lineItemId: Long) = APIAuthAction { req =>
    Logger.info(s"Rejecting traffic driver $lineItemId for campaign $campaignId")
    LineItemSummary.rejectSuggestedTrafficDriver(campaignId, lineItemId)
    NoContent
  }

  def getCampaignTrafficDriverStats(campaignId: String) = APIAuthAction { req =>
    Logger.info(s"Loading traffic driver stats for campaign $campaignId")
    Ok(toJson(TrafficDriverGroupStats.forCampaign(campaignId)))
  }

  def getCampaignCtaStats(campaignId: String) = APIAuthAction { req =>
    Ok(toJson(CtaClicksReport.getCtaClicksForCampaign(campaignId)))
  }
}

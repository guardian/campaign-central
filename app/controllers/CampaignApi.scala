package controllers

import model._
import model.command.CommandError._
import model.command.{ImportCampaignFromCAPICommand, RefreshCampaignFromCAPICommand}
import model.reports._
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import repositories._
import services.{AWS, CampaignService, Config}

class CampaignApi(
  override val wsClient: WSClient,
  components: ControllerComponents,
  val aws: AWS,
  val config: Config,
  campaignService: CampaignService,
  campaignRepository: CampaignRepository,
  campaignContentRepository: CampaignContentRepository,
  campaignNotesRepository: CampaignNotesRepository,
  campaignReferralRepository: CampaignReferralRepository,
  clientRepository: ClientRepository,
  trafficDriverRejectRepository: TrafficDriverRejectRepository,
  analyticsDataCache: AnalyticsDataCache,
  googleAnalytics: GoogleAnalytics,
  contentApi: ContentApi,
  tagManagerApi: TagManagerApi
) extends CentralController(components)
  with PandaAuthActions {

  def getCampaign(id: String) = APIAuthAction {
    campaignRepository.getCampaign(id) map { c =>
      Ok(Json.toJson(c))
    } getOrElse NotFound
  }

  def getAllCampaigns() = APIAuthAction {
    Ok(Json.toJson(campaignRepository.getAllCampaigns()))
  }

  def getLatestCampaignAnalytics() = APIAuthAction {
    Ok(Json.toJson(campaignService.getLatestCampaignAnalytics()))
  }

  def getLatestAnalyticsForCampaign(campaignId: String) = APIAuthAction {
    Ok(Json.toJson(campaignService.getLatestAnalyticsForCampaign(campaignId)))
  }

  def updateCampaign(id: String) = APIAuthAction { req =>
    req.body.asJson.flatMap(_.asOpt[Campaign]) match {
      case None => BadRequest("Could not convert json to campaign")
      case Some(campaign) =>
        campaignRepository.putCampaign(campaign)
        Ok(Json.toJson(campaign))
    }
  }

  def deleteCampaign(id: String) = APIAuthAction { _ =>
    campaignNotesRepository.deleteNotesForCampaign(id)
    campaignContentRepository.deleteContentForCampaign(id)
    campaignRepository.deleteCampaign(id)
    NoContent
  }

  def getCampaignPageViews(id: String) = APIAuthAction { _ =>
    CampaignPageViewsReport
      .getCampaignPageViewsReport(campaignRepository, analyticsDataCache, googleAnalytics, id)
      .map { c =>
        Ok(Json.toJson(c))
      } getOrElse NotFound
  }

  def getCampaignPageViewsFromDatalake(id: String) = APIAuthAction { _ =>
    val pageViews = campaignService.getPageViews(id)
    Ok(Json.toJson(pageViews))
  }

  def getCampaignUniquesFromDatalake(id: String) = APIAuthAction { _ =>
    campaignService.getUniquesDataForGraph(id).map(uniquesData => Ok(Json.toJson(uniquesData))) getOrElse NotFound
  }

  def getCampaignDailyUniqueUsers(id: String) = APIAuthAction { _ =>
    DailyUniqueUsersReport.getDailyUniqueUsersReport(campaignRepository, analyticsDataCache, googleAnalytics, id).map {
      c =>
        Ok(Json.toJson(c))
    } getOrElse NotFound
  }

  def getCampaignQualifiedPercentagesReport(id: String) = APIAuthAction { _ =>
    QualifiedPercentagesReport
      .getQualifiedPercentagesReportForCampaign(campaignRepository, analyticsDataCache, googleAnalytics, id)
      .map { c =>
        Ok(Json.toJson(c))
      } getOrElse NotFound
  }

  def getCampaignTargetsReport(id: String) = APIAuthAction { _ =>
    Ok(
      Json.toJson(
        CampaignTargetsReport.getCampaignTargetsReport(campaignRepository, id).getOrElse(CampaignTargetsReport(Map()))
      ))
  }

  def getCampaignContent(id: String) = APIAuthAction { _ =>
    Ok(Json.toJson(campaignContentRepository.getContentForCampaign(id)))
  }

  def getCampaignNotes(id: String) = APIAuthAction { _ =>
    Ok(Json.toJson(campaignNotesRepository.getNotesForCampaign(id)))
  }

  def addCampaignNote(id: String) = APIAuthAction { req =>
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

      campaignNotesRepository.putNote(newNote)
      Ok(Json.toJson(newNote))
    }
  }

  def updateCampaignNote(id: String, date: String): Action[AnyContent] = {

    APIAuthAction { req =>
      val dateCreated = new DateTime(date.toLong)

      campaignNotesRepository.getNote(id, dateCreated) match {
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

          campaignNotesRepository.putNote(updatedNote)
          Ok(Json.toJson(updatedNote))
      }
    }
  }

  def importFromTag() = APIAuthAction { req =>
    implicit val user: Option[User] = Option(User(req.user))
    req.body.asJson map { json =>
      json
        .as[ImportCampaignFromCAPICommand]
        .process(campaignRepository, campaignContentRepository, clientRepository, contentApi, tagManagerApi) match {
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
    implicit val user: Option[User] = Option(User(req.user))
    RefreshCampaignFromCAPICommand(campaignId)
      .process(campaignRepository, campaignContentRepository, contentApi, tagManagerApi) match {
      case Left(e) =>
        commandErrorAsResult(e)
      case Right(campaign) =>
        campaign map (t => Ok(Json.toJson(t))) getOrElse NotFound
    }
  }

  def getCampaignTrafficDrivers(campaignId: String) = APIAuthAction { _ =>
    Logger.info(s"Loading traffic drivers for campaign $campaignId")
    Ok(toJson(TrafficDriverGroup.forCampaign(config, campaignRepository, campaignId)))
  }

  def getSuggestedCampaignTrafficDrivers(campaignId: String) = APIAuthAction { _ =>
    Logger.info(s"Loading suggested traffic drivers for campaign $campaignId")
    val summary = LineItemSummary.suggestedTrafficDriversForCampaign(
      config,
      campaignRepository,
      clientRepository,
      trafficDriverRejectRepository,
      campaignId
    )
    Ok(toJson(summary))
  }

  def acceptSuggestedCampaignTrafficDriver(campaignId: String, lineItemId: Long) = APIAuthAction { _ =>
    Logger.info(s"Accepting traffic driver $lineItemId for campaign $campaignId")
    LineItemSummary.acceptSuggestedTrafficDriver(config, campaignId, lineItemId)
    NoContent
  }

  def rejectSuggestedCampaignTrafficDriver(campaignId: String, lineItemId: Long) = APIAuthAction { _ =>
    Logger.info(s"Rejecting traffic driver $lineItemId for campaign $campaignId")
    LineItemSummary.rejectSuggestedTrafficDriver(trafficDriverRejectRepository, campaignId, lineItemId)
    NoContent
  }

  def getCampaignTrafficDriverStats(campaignId: String) = APIAuthAction { _ =>
    Logger.info(s"Loading traffic driver stats for campaign $campaignId")
    Ok(toJson(TrafficDriverGroupStats.forCampaign(config, campaignRepository, analyticsDataCache, campaignId)))
  }

  def getCampaignCtaStats(campaignId: String) = APIAuthAction { _ =>
    Ok(
      toJson(
        CtaClicksReport.getCtaClicksForCampaign(campaignRepository, analyticsDataCache, googleAnalytics, campaignId)))
  }

  def getCampaignReferrals(campaignId: String) = APIAuthAction { _ =>
    Logger.info(s"Loading on-platform referrals for campaign $campaignId")
    Ok(toJson(CampaignReferral.forCampaign(campaignReferralRepository, campaignId)))
  }
}

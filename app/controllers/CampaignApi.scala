package controllers

import java.time.LocalDate

import com.gu.googleauth.AuthAction
import model._
import play.api.mvc._
import repositories._
import services.CampaignService
import play.api.libs.json.{JsValue, Json}

import scala.util.Try

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

  def getAllCampaigns() = authAction { implicit request =>
    val territory = Territory(request.getQueryString("territory") getOrElse "global")

    CampaignRepository.getAllCampaigns(territory) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(campaigns)              => Ok(Json.toJson(campaigns))
    }
  }

  def getBenchmarksAcrossCampaigns() = authAction { implicit request =>
    val territory = Territory(request.getQueryString("territory") getOrElse "global")

    CampaignService.getBenchmarksAcrossCampaigns(territory) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(benchmarks)             => Ok(Json.toJson(benchmarks))
    }

  }

  def getLatestCampaignAnalytics() = authAction { implicit request =>
    val territory = Territory(request.getQueryString("territory") getOrElse "global")

    CampaignService.getLatestCampaignAnalytics(territory) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(analytics)              => Ok(Json.toJson(analytics))
    }
  }

  def getLatestAnalyticsForCampaign(campaignId: String) = authAction { implicit request =>
    val territory = Territory(request.getQueryString("territory") getOrElse "global")

    CampaignService.getLatestAnalyticsForCampaign(campaignId, territory) match {
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

  def getCampaignPageViewsFromDatalake(id: String) = authAction { implicit request =>
    val territory = Territory(request.getQueryString("territory") getOrElse "global")

    CampaignService.getPageViews(id, territory) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(pageViews)              => Ok(Json.toJson(pageViews))
    }
  }

  // TODO - fix return types
  def getCampaignUniquesFromDatalake(id: String) = authAction { implicit request =>
    val territory = Territory(request.getQueryString("territory") getOrElse "global")
    CampaignService
      .getUniquesDataForGraph(id, territory)
      .map(uniquesData => Ok(Json.toJson(uniquesData))) getOrElse NotFound
  }

  def getCampaignContent(id: String) = authAction { _ =>
    CampaignContentRepository.getContentForCampaign(id) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(content)                => Ok(Json.toJson(content))
    }
  }

  def refreshCampaignFromCAPI(campaignId: String) = authAction { req =>
    implicit val user: User = User(req.user)
    CampaignService.refreshCampaignById(campaignId) match {
      case Right(campaign)                 => Ok(Json.toJson(campaign))
      case Left(CampaignNotFound(message)) => NotFound(message)
      case Left(_)                         => InternalServerError
    }
  }

  private def getReferrals[R](campaignId: String,
                              start: Option[String],
                              end: Option[String],
                              territory: Option[String])(
    fetch: (String, Option[DateRange], Option[Territory]) => Either[CampaignCentralApiError, Seq[R]])(
    toJson: Seq[R] => JsValue) = authAction { _ =>
    def toDate(o: Option[String]): Option[LocalDate] = o flatMap { s =>
      Try(LocalDate.parse(s)).toOption
    }
    val dateRange = for {
      from <- toDate(start)
      to   <- toDate(end)
    } yield DateRange(from, to)
    fetch(campaignId, dateRange, territory.map(Territory(_))) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(_)                       => InternalServerError
      case Right(referrals)              => Ok(toJson(referrals))
    }
  }

  def getOnPlatformReferrals(campaignId: String,
                             start: Option[String],
                             end: Option[String],
                             territory: Option[String]): Action[AnyContent] =
    getReferrals(campaignId, start, end, territory)(CampaignReferralRepository.getOnPlatformReferrals)(Json.toJson(_))

  def getSocialReferrals(campaignId: String,
                         start: Option[String],
                         end: Option[String],
                         territory: Option[String]): Action[AnyContent] =
    getReferrals(campaignId, start, end, territory)(CampaignReferralRepository.getSocialReferrals)(Json.toJson(_))

  def getCampaignMediaEvents(campaignId: String) = authAction { _ =>
    CampaignMediaEventsRepository.getCampaignMediaEvents(campaignId) match {
      case Left(JsonParsingError(error)) => InternalServerError(error)
      case Left(CampaignNotFound(error)) => NotFound(error)
      case Left(_)                       => InternalServerError
      case Right(mediaEvents)            => Ok(Json.toJson(mediaEvents))
    }
  }
}

package controllers

import java.util.UUID

import model._
import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import com.gu.pandomainauth.model.{User => PandaUser}
import repositories.{CampaignRepository, ClientRepository, GoogleAnalytics}

class CampaignApi(override val wsClient: WSClient) extends Controller with PandaAuthActions {

  def getAllCampaigns() = APIAuthAction { req =>
    Ok(Json.toJson(CampaignRepository.getAllCampaigns()))
  }

  def getCampaign(id: String) = APIAuthAction { req =>
    CampaignRepository.getCampaign(id) map { c => Ok(Json.toJson(c))} getOrElse NotFound
  }

  def updateCampaign(id: String) = APIAuthAction { req =>
    req.body.asJson.flatMap(_.asOpt[Campaign]) match {
      case None => BadRequest("Could not convert json to campaign")
      case Some(campaign) => {
        CampaignRepository.putCampaign(campaign)
        Ok(Json.toJson(campaign))
      }
    }
  }

  def getCampaignAnalytics(id: String) = APIAuthAction { req =>
    GoogleAnalytics.getAnalyticsForCampaign(id).flatten map { c => Ok(Json.toJson(c)) } getOrElse NotFound
  }

  def bootstrapData() = APIAuthAction { req =>

    val user = loggedInUser(req.user)
    val now = new DateTime

    val randomClient = ClientRepository.getRandomClient().get

    val campaigns = List(
      Campaign(
        id = UUID.randomUUID().toString,
        name = "Something about cars",
        status = "live",
        clientId = randomClient.id,
        created = now,
        createdBy = user,
        lastModified = now,
        lastModifiedBy = user,
        nominalValue = Some(10000),
        actualValue = Some(0),
        targets = Map("uniques" -> 10000L)
      ),Campaign(
        id = UUID.randomUUID().toString,
        name = "Pure hate",
        status = "prospect",
        clientId = randomClient.id,
        created = now,
        createdBy = user,
        lastModified = now,
        lastModifiedBy = user,
        nominalValue = Some(10000),
        actualValue = Some(0),
        targets = Map("uniques" -> 10000L)
      ),Campaign(
        id = UUID.randomUUID().toString,
        name = "TBC",
        status = "production",
        clientId = randomClient.id,
        created = now,
        createdBy = user,
        lastModified = now,
        lastModifiedBy = user,
        nominalValue = Some(10000),
        actualValue = Some(0),
        targets = Map("uniques" -> 10000L)
      ),Campaign(
        id = UUID.randomUUID().toString,
        name = "I love it when a plan comes together",
        status = "dead",
        clientId = randomClient.id,
        created = now,
        createdBy = user,
        lastModified = now,
        lastModifiedBy = user,
        nominalValue = Some(10000),
        actualValue = Some(0),
        targets = Map("uniques" -> 10000L)
      )
    )

    campaigns foreach CampaignRepository.putCampaign

    Ok("added 4 example campaigns")
  }

  def loggedInUser(pandaUser: PandaUser) = User(pandaUser.firstName, pandaUser.lastName, pandaUser.email)
}

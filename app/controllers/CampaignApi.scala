package controllers

import java.util.UUID

import model._
import model.command.CommandError._
import model.command.{ImportCampaignFromCAPICommand, RefreshCampaignFromCAPICommand}
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repositories._

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
    GoogleAnalytics.getAnalyticsForCampaign(id).map { c => Ok(Json.toJson(c)) } getOrElse NotFound
  }

  def getCampaignContent(id: String) =  APIAuthAction { req =>
    Ok(Json.toJson(CampaignContentRepository.getContentForCampaign(id)))
  }

  def getCampaignNotes(id: String) =  APIAuthAction { req =>
    Ok(Json.toJson(CampaignNotesRepository.getNotesForCampaign(id)))
  }

  def addCampaignNote(id: String) = APIAuthAction { req =>

    val content = (req.body.asJson.get \ "content").as[String]

    if (content.isEmpty())
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
        case Some(note) => {

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
  }

  def importFromTag() = APIAuthAction { req =>
    implicit val user = Option(User(req.user))
    req.body.asJson.map { json =>
      try {
        json.as[ImportCampaignFromCAPICommand].process.map{ t => Ok(Json.toJson(t)) } getOrElse NotFound
      } catch {
        commandErrorAsResult
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def refreshCampaignFromCAPI(campaignId: String) = APIAuthAction { req =>
    implicit val user = Option(User(req.user))
    try {
      RefreshCampaignFromCAPICommand(campaignId).process.map{ t => Ok(Json.toJson(t)) } getOrElse NotFound
    } catch {
      commandErrorAsResult
    }
  }

  def bootstrapData() = APIAuthAction { req =>

    val user = User(req.user)
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

    val firstCampaignId = campaigns.head.id
    val oneSecondLater = now.plusSeconds(1)

    val notes = List(
      Note(
        campaignId = firstCampaignId,
        created = now,
        createdBy = user,
        lastModified = now,
        lastModifiedBy = user,
        content = "This is a note"
      ),
      Note(
        campaignId = firstCampaignId,
        created = oneSecondLater,
        createdBy = user,
        lastModified = oneSecondLater,
        lastModifiedBy = user,
        content = "This is another note"
      )
    )

    notes foreach( CampaignNotesRepository.putNote )

    Ok("added 4 example campaigns and 2 example notes")
  }

  def getCampaignTrafficDrivers(campaignId: String) = APIAuthAction { req =>
    Logger.info(s"Loading traffic drivers for campaign $campaignId")
    Ok(toJson(TrafficDriverGroup.forCampaign(campaignId)))
  }

  def getCampaignTrafficDriverStats(campaignId: String) = APIAuthAction { req =>
    Logger.info(s"Loading traffic driver stats for campaign $campaignId")
    Ok(toJson(TrafficDriverGroupStats.forCampaign(campaignId)))
  }
}

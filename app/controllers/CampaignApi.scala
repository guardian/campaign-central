package controllers

import java.util.UUID

import model._
import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import com.gu.pandomainauth.model.{User => PandaUser}
import repositories.{CampaignNotesRepository, CampaignRepository, GoogleAnalytics}

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

  def getCampaignNotes(id: String) =  APIAuthAction { req =>
    Ok(Json.toJson(CampaignNotesRepository.getNotesForCampaign(id)))
  }

  def addCampaignNote(id: String) = APIAuthAction { req =>

    val content = (req.body.asJson.get \ "content").as[String]
    val created = DateTime.now()
    val lastModified = created
    val createdBy = User(req.user.firstName, req.user.lastName, req.user.email)
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

  def updateCampaignNote(id: String) = APIAuthAction { req =>
    println("now updating campaing note with req ", req)

    val dateCreated = (req.body.asJson.get \ "created").as[DateTime]

    CampaignNotesRepository.getNote(id, dateCreated) match {
      case None => BadRequest(s"Could not find note with id $id and create time $dateCreated")
      case Some(note) => {
        println("got the note from the repo ", note)

        val lastModified = DateTime.now()
        val modifiedBy = User(req.user.firstName, req.user.lastName, req.user.email)
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

  def bootstrapData() = APIAuthAction { req =>

    val user = loggedInUser(req.user)
    val now = new DateTime

    val campaigns = List(
      Campaign(
        id = UUID.randomUUID().toString,
        name = "Something about cars",
        status = "live",
        client = Client(UUID.randomUUID().toString, "Carmaker", "UK", Some(Agency(UUID.randomUUID().toString, "OMG"))),
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
        client = Client(UUID.randomUUID().toString, "Nigel Trump", "UK", Some(Agency(UUID.randomUUID().toString, "Evil"))),
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
        client = Client(UUID.randomUUID().toString, "Babylon Zoo", "UK", Some(Agency(UUID.randomUUID().toString, "Local Host"))),
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
        client = Client(UUID.randomUUID().toString, "A Team", "UK", Some(Agency(UUID.randomUUID().toString, "AWOL"))),
        created = now,
        createdBy = user,
        lastModified = now,
        lastModifiedBy = user,
        nominalValue = Some(10000),
        actualValue = Some(0),
        targets = Map("uniques" -> 10000L)
      )
    )

    campaigns foreach( CampaignRepository.putCampaign )

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

  def loggedInUser(pandaUser: PandaUser) = User(pandaUser.firstName, pandaUser.lastName, pandaUser.email)
}

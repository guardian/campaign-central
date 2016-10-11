package controllers

import java.util.UUID

import com.gu.pandomainauth.model.{User => PandaUser}
import model._
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repositories.{ClientRepository}

class ClientApi(override val wsClient: WSClient) extends Controller with PandaAuthActions {

  def getAllClients() = APIAuthAction { req =>
    Ok(Json.toJson(ClientRepository.getAllClients()))
  }

  def getClient(id: String) = APIAuthAction { req =>
    ClientRepository.getClient(id) map { c => Ok(Json.toJson(c))} getOrElse NotFound
  }

  def updateClient(id: String) = APIAuthAction { req =>
    req.body.asJson.flatMap(_.asOpt[Client]) match {
      case None => BadRequest("Could not convert json to client")
      case Some(client) => {
        ClientRepository.putClient(client)
        Ok(Json.toJson(client))
      }
    }
  }


  def bootstrapData() = APIAuthAction { req =>

    val user = User(req.user)
    val now = new DateTime

    val clients = List(
      Client(
        id = UUID.randomUUID().toString,
        name = "Renault",
        country = "UK"
      ),
      Client(
        id = UUID.randomUUID().toString,
        name = "Singapore Airlines",
        country = "SG"
      ),
      Client(
        id = UUID.randomUUID().toString,
        name = "Ford",
        country = "US"
      ),
      Client(
        id = UUID.randomUUID().toString,
        name = "Chester Zoo",
        country = "UK"
      ),
      Client(
        id = UUID.randomUUID().toString,
        name = "Leffe",
        country = "UK"
      )
    )

    clients foreach( ClientRepository.putClient )

    Ok("added 5 example clients")
  }

}

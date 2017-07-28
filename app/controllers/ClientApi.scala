package controllers

import model._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.Controller
import repositories.ClientRepository

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

}

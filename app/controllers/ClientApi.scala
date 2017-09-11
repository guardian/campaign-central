package controllers

import model._
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.ControllerComponents
import repositories.ClientRepository
import services.{AWS, Config}

class ClientApi(
  override val wsClient: WSClient,
  components: ControllerComponents,
  val aws: AWS,
  val config: Config,
  clientRepository: ClientRepository
) extends CentralController(components)
  with PandaAuthActions {

  def getAllClients() = APIAuthAction { _ =>
    Ok(Json.toJson(clientRepository.getAllClients()))
  }

  def getClient(id: String) = APIAuthAction { _ =>
    clientRepository.getClient(id) map { c =>
      Ok(Json.toJson(c))
    } getOrElse NotFound
  }

  def updateClient(id: String) = APIAuthAction { req =>
    req.body.asJson.flatMap(_.asOpt[Client]) match {
      case None => BadRequest("Could not convert json to client")
      case Some(client) =>
        clientRepository.putClient(client)
        Ok(Json.toJson(client))
    }
  }

}

package controllers

import com.gu.googleauth.AuthAction
import model._
import play.api.libs.json._
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents}
import repositories.ClientRepository

class ClientApi(components: ControllerComponents, authAction: AuthAction[AnyContent])
  extends AbstractController(components) {

  def getAllClients() = authAction { _ =>
    Ok(Json.toJson(ClientRepository.getAllClients()))
  }

  def getClient(id: String) = authAction { _ =>
    ClientRepository.getClient(id) map { c =>
      Ok(Json.toJson(c))
    } getOrElse NotFound
  }

  def updateClient(id: String) = authAction { req =>
    req.body.asJson.flatMap(_.asOpt[Client]) match {
      case None => BadRequest("Could not convert json to client")
      case Some(client) =>
        ClientRepository.putClient(client)
        Ok(Json.toJson(client))
    }
  }

}

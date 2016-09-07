package repositories

import model.Client
import play.api.Logger
import services.Dynamo
import scala.collection.JavaConversions._

object ClientRepository {

  def getClient(clientId: String) = {
    Option(Dynamo.clientTable.getItem("id", clientId)).map{ Client.fromItem }
  }

  def getAllClients() = {
    Dynamo.clientTable.scan().map{ Client.fromItem }.toList
  }

  def putClient(client: Client) = {
    try {
      Dynamo.clientTable.putItem(client.toItem)
      Some(client)
    } catch {
      case e: Error => {
        Logger.error(s"failed to persist client $client", e)
        None
      }
    }
  }

}

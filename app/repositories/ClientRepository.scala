package repositories

import com.amazonaws.services.dynamodbv2.document.ScanFilter
import model.Client
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._
import scala.util.Random

class ClientRepository(dynamo: Dynamo) {

  def getClient(clientId: String): Option[Client] = {
    Option(dynamo.clientTable.getItem("id", clientId)).map { Client.fromItem }
  }

  def getClientByName(clientName: String): Option[Client] = {
    val filter: ScanFilter = new ScanFilter("name").eq(clientName)
    dynamo.clientTable.scan(filter).headOption.map { Client.fromItem }
  }

  def getAllClients(): List[Client] = {
    dynamo.clientTable.scan().map { Client.fromItem }.toList
  }

  def putClient(client: Client): Option[Client] = {
    try {
      dynamo.clientTable.putItem(client.toItem)
      Some(client)
    } catch {
      case e: Error =>
        Logger.error(s"failed to persist client $client", e)
        None
    }
  }

  def getRandomClient(): Option[Client] = { //This is used for bootstrapping purposes, shouldn't be relied upon.
    Random.shuffle(getAllClients()).headOption
  }

}

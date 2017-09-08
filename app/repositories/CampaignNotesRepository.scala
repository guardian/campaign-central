package repositories

import model.Note
import org.joda.time.DateTime
import play.api.Logger
import services.Dynamo

import scala.collection.JavaConversions._

class CampaignNotesRepository(dynamo: Dynamo) {

  def getNotesForCampaign(campaignId: String): List[Note] = {
    dynamo.campaignNotesTable.query("campaignId", campaignId).map { Note.fromItem }.toList
  }

  def getNote(campaignId: String, created: DateTime): Option[Note] = {
    Option(dynamo.campaignNotesTable.getItem("campaignId", campaignId, "created", created.getMillis)).map {
      Note.fromItem
    }
  }

  def deleteNotesForCampaign(campaignId: String): Unit = {
    try {
      for (note <- dynamo.campaignNotesTable.query("campaignId", campaignId)) {
        dynamo.campaignNotesTable.deleteItem("campaignId", campaignId, "created", note.getNumber("created"))
      }
    } catch {
      case e: Error =>
        Logger.error(s"failed to delete notes for campaign $campaignId", e)
        None
    }
  }

  def putNote(note: Note): Option[Note] = {
    try {
      dynamo.campaignNotesTable.putItem(note.toItem)
      Some(note)
    } catch {
      case e: Error =>
        Logger.error(s"failed to persist note $note", e)
        None
    }
  }

}

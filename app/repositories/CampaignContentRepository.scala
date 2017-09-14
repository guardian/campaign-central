package repositories

import com.gu.scanamo.Scanamo
import com.gu.scanamo.syntax._
import model.ContentItem
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

object CampaignContentRepository {

  private implicit val logger: Logger = Logger(getClass)

  private val tableName = Config().campaignContentTableName

  def getContentForCampaign(campaignId: String): List[ContentItem] =
    getResults(Scanamo.query[ContentItem](DynamoClient)(tableName)('campaignId -> campaignId))

  def deleteContentForCampaign(campaignId: String): Unit =
    getResults(Scanamo.query[ContentItem](DynamoClient)(tableName)('campaignId -> campaignId)) foreach { content =>
      Scanamo.delete(DynamoClient)(tableName)('campaignId -> campaignId and 'id -> content.id)
    }

  def putContent(content: ContentItem): Unit = Scanamo.put(DynamoClient)(tableName)(content)
}

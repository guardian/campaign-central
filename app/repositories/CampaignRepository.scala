package repositories

import com.amazonaws.services.dynamodbv2.model.DeleteItemResult
import com.gu.scanamo.Scanamo
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.query.UniqueKey
import com.gu.scanamo.syntax._
import model.{Campaign, CampaignWithSubItems}
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.{getResult, getResults}

import scala.util.Try
import com.amazonaws.services.dynamodbv2.model.PutItemResult
import com.gu.scanamo.Scanamo
import com.gu.scanamo.error.DynamoReadError
import com.gu.scanamo.syntax._
import model.ContentItem
import play.api.Logger
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResults

object CampaignRepository {

  private implicit val logger: Logger = Logger(getClass)

  private val tableName = Config().campaignTableName

  private def getCampaignByKey(key: UniqueKey[_]): Option[Campaign] = {
    val x: Option[Either[DynamoReadError, Campaign]] = Scanamo.get(DynamoClient)(tableName)(key)
    val w                                            = x flatMap (h => getResult(h))
    w
  }

  def getCampaign(campaignId: String): Option[Campaign] =
    getCampaignByKey('campaignId -> campaignId)

//  def getCampaignByTag(tagId: Long): Option[Campaign] = {
  def getCampaignByTag(tagId: String): Option[Campaign] = {
    val tuple: UniqueKey[_] = 'tagId -> tagId
    val option: Option[Either[DynamoReadError, Campaign]] =
      Scanamo.get[Campaign](DynamoClient)(tableName)(tuple)
    val h = option.flatMap(o => getResult(o))
    h
  }

  def getAllCampaigns(): Seq[Campaign] = getResults(Scanamo.scan[Campaign](DynamoClient)(tableName))

  def getCampaignWithSubItems(campaignId: String): Option[CampaignWithSubItems] = {

    val campaign = getCampaign(campaignId)

    campaign map { c =>
      CampaignWithSubItems(
        campaign = c,
        content = CampaignContentRepository.getContentForCampaign(c.id)
      )
    }
  }

  def deleteCampaign(campaignId: String): DeleteItemResult =
    Scanamo.delete(DynamoClient)(tableName)('campaignId -> campaignId)

  def putCampaign(campaign: Campaign): Option[Campaign] =
    Try(Scanamo.put[Campaign](DynamoClient)(tableName)(campaign)).toOption.map(_ => campaign)
}

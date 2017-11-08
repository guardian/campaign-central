package repositories

import cats.implicits._
import com.gu.scanamo._
import com.gu.scanamo.syntax._
import model._
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignReferralRepository {

  private val table = Table[CampaignReferralRow](Config().campaignReferralTableName)

  def getCampaignReferrals(campaignId: String): Either[CampaignCentralApiError, Seq[CampaignReferral]] = {
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(table.query('campaignId -> campaignId))) match {
      case Left(e)     => Left(JsonParsingError(e.show))
      case Right(rows) => Right(CampaignReferral.fromRows(rows))
    }
  }
}

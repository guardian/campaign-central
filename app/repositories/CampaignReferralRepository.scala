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

  def getCampaignReferrals(campaignId: String,
                           dateRange: Option[DateRange]): Either[CampaignCentralApiError, Seq[CampaignReferral]] = {
    val query = dateRange map { range =>
      val fromStr = range.from.toString
      val toStr   = range.to.toString
      table.filter('referralDate >= fromStr and 'referralDate <= toStr).query('campaignId -> campaignId)
    } getOrElse
      table.query('campaignId -> campaignId)
    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(query)) match {
      case Left(e)     => Left(JsonParsingError(e.show))
      case Right(rows) => Right(CampaignReferral.fromRows(rows))
    }
  }
}

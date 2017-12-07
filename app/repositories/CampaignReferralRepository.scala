package repositories

import cats.implicits._
import com.gu.scanamo._
import com.gu.scanamo.syntax._
import model._
import services.AWS.DynamoClient
import services.Config
import util.DynamoResults.getResultsOrFirstFailure

object CampaignReferralRepository {

  private val onPlatformTable = Table[OnPlatformReferralRow](Config().onPlatformReferralTableName)
  private val socialTable     = Table[SocialReferralRow](Config().socialReferralTableName)

  private def getReferrals[A, R](
    table: Table[A],
    campaignId: String,
    dateRange: Option[DateRange],
    territory: Option[Territory])(toReferrals: Seq[A] => Seq[R]): Either[CampaignCentralApiError, Seq[R]] = {

    def dateRangeFilter(r: DateRange) = {
      val fromStr = r.from.toString
      val toStr   = r.to.toString
      'referralDate >= fromStr and 'referralDate <= toStr
    }

    def territoryFilter(t: Territory) = 'territory -> t.databaseKeyValue

    val query = (dateRange, territory.filterNot(_ == Global)) match {
      case (Some(r), Some(t)) =>
        table.filter(dateRangeFilter(r) and territoryFilter(t)).query('campaignId -> campaignId)
      case (Some(r), None) =>
        table.filter(dateRangeFilter(r)).query('campaignId -> campaignId)
      case (None, Some(t)) =>
        table.filter(territoryFilter(t)).query('campaignId -> campaignId)
      case (None, None) =>
        table.query('campaignId -> campaignId)
    }

    getResultsOrFirstFailure(Scanamo.exec(DynamoClient)(query)) match {
      case Left(e)     => Left(JsonParsingError(e.show))
      case Right(rows) => Right(toReferrals(rows))
    }
  }

  def getOnPlatformReferrals(campaignId: String,
                             dateRange: Option[DateRange],
                             territory: Option[Territory]): Either[CampaignCentralApiError, Seq[OnPlatformReferral]] =
    getReferrals(onPlatformTable, campaignId, dateRange, territory)(OnPlatformReferral.fromRows)

  def getSocialReferrals(campaignId: String,
                         dateRange: Option[DateRange],
                         territory: Option[Territory]): Either[CampaignCentralApiError, Seq[SocialReferral]] =
    getReferrals(socialTable, campaignId, dateRange, territory)(SocialReferral.fromRows)
}

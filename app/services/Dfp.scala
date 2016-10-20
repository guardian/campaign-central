package services

import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api._
import com.google.api.ads.dfp.axis.factory.DfpServices
import com.google.api.ads.dfp.axis.utils.v201608.StatementBuilder
import com.google.api.ads.dfp.axis.v201608._
import com.google.api.ads.dfp.lib.client.DfpSession
import services.Config.conf._

object DfpFetcher {

  def mkSession(): DfpSession = {
    new DfpSession.Builder()
    .withOAuth2Credential(
      new OfflineCredentials.Builder()
      .forApi(DFP)
      .withClientSecrets(dfpClientId, dfpClientSecret)
      .withRefreshToken(dfpRefreshToken)
      .build()
      .generateCredential()
    )
    .withApplicationName(dfpAppName)
    .withNetworkCode(dfpNetworkCode)
    .build()
  }

  def fetchLineItemsByOrder(session: DfpSession, orderId: Long): Seq[LineItem] =
    fetchLineItems(
      session,
      new StatementBuilder()
      .where("orderId = :orderId")
      .withBindVariableValue("orderId", orderId)
      .toStatement
    )

  private def fetchLineItems(session: DfpSession, statement: Statement): Seq[LineItem] = {

    val lineItemService = new DfpServices().get(session, classOf[LineItemServiceInterface])

    val lineItemPage = lineItemService.getLineItemsByStatement(statement)

    // assuming only one page of results
    lineItemPage.getResults.toSeq
  }
}

object DfpFilter {

  def hasCampaignIdCustomFieldValue(campaignId: String)(lineItem: LineItem): Boolean = {
    safeSeq(lineItem.getCustomFieldValues) exists { value =>
      value.getCustomFieldId == dfpCampaignFieldId &&
      value.asInstanceOf[CustomFieldValue].getValue.asInstanceOf[TextValue].getValue.toLowerCase == campaignId
    }
  }

  private def safeSeq[T](ts: Array[T]): Seq[T] = Option(ts).map(_.toSeq).getOrElse(Nil)
}

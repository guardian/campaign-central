package services

import java.time.LocalDate

import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api._
import com.google.api.ads.dfp.axis.factory.DfpServices
import com.google.api.ads.dfp.axis.utils.v201608.StatementBuilder
import com.google.api.ads.dfp.axis.v201608._
import com.google.api.ads.dfp.lib.client.DfpSession
import model.TrafficDriver
import services.Config.conf._

object Dfp {

  def fetchTrafficDriversByCampaign(id: String): Seq[TrafficDriver] = {

    def mkTrafficDriver(driverType: String)(lineItem: LineItem) = {

      def mkLocalDate(dfpDateTime: DateTime): LocalDate = {
        val date = dfpDateTime.getDate
        LocalDate.of(date.getYear, date.getMonth, date.getDay)
      }

      TrafficDriver(
        id = lineItem.getId,
        name = lineItem.getName,
        url = s"https://www.google.com/dfp/$dfpNetworkCode#delivery/LineItemDetail/lineItemId=${lineItem.getId}",
        driverType,
        status = lineItem.getStatus.getValue,
        startDate = mkLocalDate(lineItem.getStartDateTime),
        endDate = mkLocalDate(lineItem.getEndDateTime),
        impressionsDelivered = lineItem.getStats.getImpressionsDelivered.toInt,
        clicksDelivered = lineItem.getStats.getClicksDelivered.toInt,
        ctrDelivered = lineItem.getStats.getClicksDelivered / lineItem.getStats.getImpressionsDelivered.toDouble * 100
      )
    }

    val nativeCardLineItems =
      fetchLineItemsByOrderAndCustomField(dfpNativeCardOrderId, dfpCampaignFieldId, id)
    val merchComponentLineItems =
      fetchLineItemsByOrderAndCustomField(dfpMerchComponentOrderId, dfpCampaignFieldId, id)

      nativeCardLineItems.map(mkTrafficDriver("Native card")) ++
      merchComponentLineItems.map(mkTrafficDriver("Merch component"))
  }

  def fetchLineItemsByOrderAndCustomField(orderId: Long, fieldId: Long, fieldValue: String): Seq[LineItem] = {

    val session = new DfpSession.Builder()
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

    val lineItemService = new DfpServices().get(session, classOf[LineItemServiceInterface])

    val lineItemPage = lineItemService.getLineItemsByStatement(
      new StatementBuilder()
      .where("orderId = :orderId")
      .withBindVariableValue("orderId", orderId)
      .toStatement
    )

    // assuming only one page of results
    val lineItems = lineItemPage.getResults.toSeq

    lineItems filter { lineItem =>
      safeSeq(lineItem.getCustomFieldValues) exists { value =>
        value.getCustomFieldId == fieldId &&
        value.asInstanceOf[CustomFieldValue].getValue.asInstanceOf[TextValue].getValue.toLowerCase == fieldValue
      }
    }
  }

  private def safeSeq[T](ts: Array[T]): Seq[T] = Option(ts).map(_.toSeq).getOrElse(Nil)
}

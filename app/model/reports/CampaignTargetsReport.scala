package model.reports

import ai.x.play.json.Jsonx
import org.joda.time.{DateTime, Duration}
import play.api.libs.json.Format
import repositories.CampaignRepository

case class CampaignTargetRunRateElement(date: DateTime, expected: Long)

object CampaignTargetRunRateElement {
  implicit val campaignTargetRunRateElemenFormat: Format[CampaignTargetRunRateElement] = Jsonx.formatCaseClass[CampaignTargetRunRateElement]
}

case class CampaignTarget(target: Long, runRate: List[CampaignTargetRunRateElement])

object CampaignTarget {
  implicit val campaignTargetFormat: Format[CampaignTarget] = Jsonx.formatCaseClass[CampaignTarget]
}

case class CampaignTargetsReport(targets: Map[String, CampaignTarget])

object CampaignTargetsReport {

  implicit val campaignTargetsReportFormat: Format[CampaignTargetsReport] = Jsonx.formatCaseClass[CampaignTargetsReport]

  def getCampaignTargetsReport(campaignId: String): Option[CampaignTargetsReport] = {
    for (
      campaign <- CampaignRepository.getCampaign(campaignId);
      startDate <- campaign.startDate;
      endDate <- campaign.endDate
    ) yield {

      val campaignLength = new Duration(startDate, endDate).getStandardDays
      val reportRange = DateBasedReport.calculateDatesToFetch(startDate, campaign.endDate)

      CampaignTargetsReport(
        campaign.targets.keys.map { targetName =>
          val target = campaign.targets(targetName)
          val dailyRunRate = target.toDouble / campaignLength

          val runRate = reportRange.map{ date =>
            val daysIn = new Duration(startDate, date).getStandardDays
            CampaignTargetRunRateElement(date, (daysIn * dailyRunRate).toLong)
          }
          targetName -> CampaignTarget(target, runRate)
        }.toMap
      )
    }
  }
}

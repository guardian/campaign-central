package services

import model.{CampaignAnalyticsLatestItem, CampaignPageViewsItem, CampaignUniquesItem, GraphDataPoint}
import model.reports.{CampaignSummary, OverallSummaryReport}
import org.joda.time.DateTime
import repositories.{CampaignAnalyticsLatestRepository, CampaignPageViewsRepository, CampaignRepository, CampaignUniquesRepository}

object CampaignService {

  def getPageViews(campaignId: String): Seq[CampaignPageViewsItem] = {
    CampaignPageViewsRepository.getCampaignPageViews(campaignId)
  }

  def getUniques(campaignId: String): Seq[CampaignUniquesItem] = {
    CampaignUniquesRepository.getCampaignUniques(campaignId)
  }

  def getLatestAnalyticsForCampaign(campaignId: String): Option[CampaignAnalyticsLatestItem] = {
    CampaignAnalyticsLatestRepository.getLatestCampaignAnalytics(campaignId)
  }

  def getUniquesDataForGraph(campaignId: String): Option[Seq[GraphDataPoint]] = {

    val campaignUniques = CampaignUniquesRepository.getCampaignUniques(campaignId)
    val initialDataPoint = campaignUniques.headOption.map { item =>
      item.copy(reportExecutionTimestamp = new DateTime(item.reportExecutionTimestamp).minusDays(1).toString, uniques = 0L)
    }

    val uniqueItems = initialDataPoint ++ campaignUniques
    val maybeTarget = CampaignRepository.getCampaign(campaignId).flatMap(_.targets.get("uniques"))

    maybeTarget map { target =>
      val runRateStep = {
        val numItems = uniqueItems.size.toLong
        if (numItems == 0) 1
        else target / numItems
      }
      val runRate = Seq.range[Long](0, target + runRateStep, runRateStep)
      (uniqueItems zip runRate).map { case (unique, rate) =>
        GraphDataPoint(
          name = unique.reportExecutionTimestamp,
          dataPoint = unique.uniques,
          target = rate
        )
      }.toSeq
    }
  }

  def getOverallSummary(): OverallSummaryReport = {
    val latestCampaignAnalytics = CampaignAnalyticsLatestRepository.getLatestCampaignAnalytics()
    val latestCampaignUniqueIds = latestCampaignAnalytics.map(_.campaignId)
    val campaignsWeHaveUniquesFor = CampaignRepository.getAllCampaigns().filter(c => latestCampaignUniqueIds.contains(c.id))

    val campaignSummaries = campaignsWeHaveUniquesFor flatMap { campaign =>
      for {
        uniques <- latestCampaignAnalytics.find(_.campaignId == campaign.id).map(_.uniques)
      } yield campaign.id -> CampaignSummary(uniques, campaign.targets.getOrElse("uniques", 0))
    }

    OverallSummaryReport(campaignSummaries.toMap)
  }
}

package services

import model.CampaignPageViewsItem
import model.CampaignUniquesItem
import repositories.CampaignPageViewsRepository
import repositories.CampaignUniquesRepository

object CampaignService {
  def getPageViews(campaignId: String): Seq[CampaignPageViewsItem] = {
    CampaignPageViewsRepository.getCampaignPageViews(campaignId)
  }
  def getUniques(campaignId: String): Seq[CampaignUniquesItem] = {
    CampaignUniquesRepository.getCampaignUniques(campaignId)
  }
}

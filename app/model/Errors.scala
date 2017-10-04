package model

sealed trait CampaignCentralApiError
case class CampaignNotFound(message: String)                                          extends CampaignCentralApiError
case class LatestCampaignAnalyticsItemNotFound(message: String)                       extends CampaignCentralApiError
case class ContentItemNotFound(message: String)                                       extends CampaignCentralApiError
case class ContentItemFailedToPersist(contentItem: ContentItem, exception: Throwable) extends CampaignCentralApiError
case class CampaignDeletionFailed(campaignId: String, exception: Throwable)           extends CampaignCentralApiError
case class CampaignItemDeletionFailed(campaignId: String, exception: Throwable)       extends CampaignCentralApiError
case class CampaignPutError(campaign: Campaign, exception: Throwable)                 extends CampaignCentralApiError
case class CampaignPutAllError(campaigns: Seq[Campaign], exception: Throwable)        extends CampaignCentralApiError
case class JsonParsingError(message: String)                                          extends CampaignCentralApiError
case class CampaignSectionNotFound(message: String)                                   extends CampaignCentralApiError

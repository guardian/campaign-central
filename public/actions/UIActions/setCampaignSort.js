export const SET_CAMPAIGN_SORT = 'SET_CAMPAIGN_SORT';

export function setCampaignSort(campaignSortColumn, campaignSortOrder) {
  console.log(campaignSortOrder);
    return {
        type:       SET_CAMPAIGN_SORT,
        campaignSortColumn:    campaignSortColumn,
        campaignSortOrder: campaignSortOrder,
        receivedAt: Date.now()
    };
}

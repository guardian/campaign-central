export const SET_CAMPAIGN_SORT = 'SET_CAMPAIGN_SORT';

export function setCampaignSort(campaignSortColumn) {
    return {
        type:       SET_CAMPAIGN_SORT,
        campaignSortColumn:    campaignSortColumn,
        receivedAt: Date.now()
    };
}


function recieveCampaignAnalytics() {
    return {
        type:        'CAMPAIGN_ANALYTICS_GET_RECIEVE',
        campaignAnalytics:    undefined,
        receivedAt:  Date.now()
    };
}

export function clearCampaignAnalytics() {
    return dispatch => {
        dispatch(recieveCampaignAnalytics());
    };
}


function receiveCampaignPageViews() {
    return {
        type:        'CAMPAIGN_PAGE_VIEWS_GET_RECEIVE',
        campaignPageViews:    undefined,
        receivedAt:  Date.now()
    };
}

function receiveCampaignDailyUniques() {
    return {
        type:        'CAMPAIGN_DAILY_UNIQUES_GET_RECEIVE',
        campaignDailyUniques:    undefined,
        receivedAt:  Date.now()
    };
}

function receiveCampaignTargetsReport() {
    return {
        type:        'CAMPAIGN_TARGETS_REPORT_GET_RECEIVE',
        campaignTargetsReport:    undefined,
        receivedAt:  Date.now()
    };
}

export function clearCampaignAnalytics() {
    return dispatch => {
        dispatch(receiveCampaignPageViews());
        dispatch(receiveCampaignDailyUniques());
        dispatch(receiveCampaignTargetsReport());
    };
}

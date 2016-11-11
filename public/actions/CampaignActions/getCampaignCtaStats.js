import {fetchCampaignCtaStats} from '../../services/CampaignsApi';

function requestCampaignCtaStats(id) {
    return {
        type:       'CAMPAIGN_CTA_STATS_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignCtaStats(campaignCtaStats) {
    return {
        type:        'CAMPAIGN_CTA_STATS_GET_RECEIVE',
        campaignCtaStats:    campaignCtaStats,
        receivedAt:  Date.now()
    };
}

function errorRecievingCampaignCtaStats(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign CTA stats',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignCtaStats(id) {
    return dispatch => {
      dispatch(requestCampaignCtaStats(id));
      return fetchCampaignCtaStats(id)
        .catch(error => dispatch(errorRecievingCampaignCtaStats(error)))
        .then(res => {
          dispatch(receiveCampaignCtaStats(res));
        });
    };
}

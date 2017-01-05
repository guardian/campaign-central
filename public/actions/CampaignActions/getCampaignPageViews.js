import {fetchCampaignPageViews} from '../../services/CampaignsApi';

function requestCampaignPageViews(id) {
    return {
        type:       'CAMPAIGN_PAGE_VIEWS_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignPageViews(campaignPageViews) {
    return {
        type:        'CAMPAIGN_PAGE_VIEWS_GET_RECEIVE',
        campaignPageViews:    campaignPageViews,
        receivedAt:  Date.now()
    };
}

function errorRecievingCampaignPageViews(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign page views',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignPageViews(id) {
    return dispatch => {
      dispatch(requestCampaignPageViews(id));
      return fetchCampaignPageViews(id)
        .catch(error => dispatch(errorRecievingCampaignPageViews(error)))
        .then(res => {
          dispatch(receiveCampaignPageViews(res));
        });
    };
}

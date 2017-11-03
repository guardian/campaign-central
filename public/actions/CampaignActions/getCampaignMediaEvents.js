import {fetchCampaignMediaEvents} from '../../services/CampaignsApi';

function requestCampaignMediaEvents(id) {
    return {
        type:       'CAMPAIGN_MEDIA_EVENTS_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignMediaEvents(campaignMediaEvents) {
    return {
        type:        'CAMPAIGN_MEDIA_EVENTS_GET_RECEIVE',
        campaignMediaEvents:    campaignMediaEvents,
        receivedAt:  Date.now()
    };
}

function errorRecievingCampaignMediaEvents(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get campaign media events',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignMediaEvents(id) {
    return dispatch => {
      dispatch(requestCampaignMediaEvents(id));
      return fetchCampaignMediaEvents(id)
        .catch(error => dispatch(errorRecievingCampaignMediaEvents(error)))
        .then(res => {
          dispatch(receiveCampaignMediaEvents(res));
        });
    };
}

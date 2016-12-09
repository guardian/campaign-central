import {fetchCampaignTrafficDriverSuggestions} from '../../services/CampaignsApi';

function requestCampaignTrafficDriverSuggestions(id) {
   return {
        type:       'TRAFFIC_DRIVER_SUGGESTIONS_GET_REQUEST',
        id:         id,
        receivedAt: Date.now()
    };
}

function receiveCampaignTrafficDriverSuggestions(trafficDriverSuggestions) {
    return {
        type:                             'TRAFFIC_DRIVER_SUGGESTIONS_GET_RECEIVE',
        campaignTrafficDriverSuggestions: trafficDriverSuggestions,
        receivedAt:                       Date.now()
    };
}

function errorReceivingCampaignTrafficDriverSuggestions(error) {
    return {
        type:       'SHOW_ERROR',
        message:    'Could not get traffic driver suggestions',
        error:      error,
        receivedAt: Date.now()
    };
}

export function getCampaignTrafficDriverSuggestions(id) {
    return dispatch => {
      dispatch(requestCampaignTrafficDriverSuggestions(id));
      return fetchCampaignTrafficDriverSuggestions(id)
        .catch(error => dispatch(errorReceivingCampaignTrafficDriverSuggestions(error)))
        .then(res => {
          dispatch(receiveCampaignTrafficDriverSuggestions(res));
        });
    };
}

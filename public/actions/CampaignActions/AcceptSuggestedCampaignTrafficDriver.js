import {acceptSuggestedCampaignTrafficDriver as acceptSuggestedCampaignTrafficDriverApi} from "../../services/CampaignsApi";
import {fetchCampaignTrafficDrivers as fetchCampaignTrafficDriversApi} from "../../services/CampaignsApi";

function requestAccept(campaignId, trafficDriverId) {
  return {
    type: 'CAMPAIGN_DRIVER_ACCEPT_REQUEST',
    campaignId: campaignId,
    trafficDriverId: trafficDriverId,
    receivedAt: Date.now()
  };
}

function receiveAccept() {
  return {
    type: 'CAMPAIGN_DRIVER_ACCEPT_RECEIVE',
    receivedAt: Date.now()
  };
}

function errorAccepting(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not accept suggested campaign traffic driver',
    error: error,
    receivedAt: Date.now()
  };
}

export function acceptSuggestedCampaignTrafficDriver(campaignId, trafficDriverId) {
  return dispatch => {
    dispatch(requestAccept(campaignId, trafficDriverId));
    return acceptSuggestedCampaignTrafficDriverApi(campaignId, trafficDriverId)
      .catch(error => dispatch(errorAccepting(error)))
      .then(res => {
//        dispatch(fetchCampaignTrafficDriversApi(campaignId));
        dispatch(receiveAccept(res));
      });
  };
}

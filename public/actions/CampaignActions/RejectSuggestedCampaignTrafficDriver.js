import {rejectSuggestedCampaignTrafficDriver as rejectSuggestedCampaignTrafficDriverApi} from "../../services/CampaignsApi";

function requestReject(campaignId, trafficDriverId) {
  return {
    type: 'CAMPAIGN_DRIVER_REJECT_REQUEST',
    campaignId: campaignId,
    trafficDriverId: trafficDriverId,
    receivedAt: Date.now()
  };
}

function receiveReject() {
  return {
    type: 'CAMPAIGN_DRIVER_REJECT_RECEIVE',
    receivedAt: Date.now()
  };
}

function errorRejecting(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not reject suggested campaign traffic driver',
    error: error,
    receivedAt: Date.now()
  };
}

export function rejectSuggestedCampaignTrafficDriver(campaignId, trafficDriverId) {
  return dispatch => {
    dispatch(requestReject(campaignId, trafficDriverId));
    return rejectSuggestedCampaignTrafficDriverApi(campaignId, trafficDriverId)
      .catch(error => dispatch(errorRejecting(error)))
      .then(res => {
        dispatch(receiveReject(res));
      });
  };
}

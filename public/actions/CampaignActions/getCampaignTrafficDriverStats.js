import {fetchCampaignTrafficDriverStats} from "../../services/CampaignsApi";

function requestCampaignTrafficDriverStats(id) {
  return {
    type: 'TRAFFIC_DRIVER_STATS_GET_REQUEST',
    id: id,
    receivedAt: Date.now()
  };
}

function receiveCampaignTrafficDriverStats(trafficDriverStats) {
  return {
    type: 'TRAFFIC_DRIVER_STATS_GET_RECEIVE',
    campaignTrafficDriverStats: trafficDriverStats,
    receivedAt: Date.now()
  };
}

function errorReceivingCampaignTrafficDriverStats(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get traffic driver stats',
    error: error,
    receivedAt: Date.now()
  };
}

export function getCampaignTrafficDriverStats(id) {
  return dispatch => {
    dispatch(requestCampaignTrafficDriverStats(id));
    return fetchCampaignTrafficDriverStats(id)
      .catch(error => dispatch(errorReceivingCampaignTrafficDriverStats(error)))
      .then(res => {
        dispatch(receiveCampaignTrafficDriverStats(res));
      });
  };
}

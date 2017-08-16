import {fetchLatestAnalyticsForCampaign} from '../../services/CampaignsApi';

function requestlatestAnalyticsForCampaign(id) {
  return {
    type:       'LATEST_ANALYTICS_FOR_CAMPAIGN_GET_REQUEST',
    id:         id,
    receivedAt: Date.now()
  };
}

function receiveLatestAnalyticsForCampaign(latestAnalyticsForCampaign) {
  return {
    type:        'LATEST_ANALYTICS_FOR_CAMPAIGN_GET_RECEIVE',
    latestAnalyticsForCampaign:    latestAnalyticsForCampaign,
    receivedAt:  Date.now()
  };
}

function errorReceivingLatestlAnalyticsForCampaign(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get latest analytics for campaign',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getLatestAnalyticsForCampaign(id) {
  return dispatch => {
    dispatch(requestlatestAnalyticsForCampaign(id));
    return fetchLatestAnalyticsForCampaign(id)
      .catch(error => dispatch(errorReceivingLatestlAnalyticsForCampaign(error)))
      .then(res => dispatch(receiveLatestAnalyticsForCampaign(res)));
  };
}

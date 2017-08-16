
export default function latestAnalyticsForCampaign(state = false, action) {
  switch (action.type) {

    case 'LATEST_ANALYTICS_FOR_CAMPAIGN_GET_RECEIVE':
      return action.latestAnalyticsForCampaign || false;

    default:
      return state;
  }
}

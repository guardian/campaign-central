
export default function campaignAnalytics(state = false, action) {
  switch (action.type) {

    case 'CAMPAIGN_ANALYTICS_GET_RECIEVE':
      return action.campaignAnalytics || false;

    default:
      return state;
  }
}

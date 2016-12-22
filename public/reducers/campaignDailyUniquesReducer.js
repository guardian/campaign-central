
export default function campaignDailyUniques(state = false, action) {
  switch (action.type) {

    case 'CAMPAIGN_DAILY_UNIQUES_GET_RECEIVE':
      return action.campaignDailyUniques || false;

    default:
      return state;
  }
}

export default function error(state = false, action) {
  switch (action.type) {
    case 'SET_CAMPAIGN_STATE_FILTER':
      return action.campaignStateFilter;

    default:
      return state;
  }
}

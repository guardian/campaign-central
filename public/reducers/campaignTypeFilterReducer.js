export default function error(state = false, action) {
  switch (action.type) {
    case 'SET_CAMPAIGN_TYPE_FILTER':
      return action.campaignTypeFilter;

    default:
      return state;
  }
}

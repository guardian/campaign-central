export default function error(state = false, action) {
  switch (action.type) {
    case 'SET_CAMPAIGN_SORT':
      return action.campaignSortColumn;

    default:
      return state;
  }
}

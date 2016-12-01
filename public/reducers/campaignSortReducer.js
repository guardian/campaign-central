export default function error(state = false, action) {
  switch (action.type) {
    case 'SET_CAMPAIGN_SORT':
      return {
        'campaignSortColumn': action.campaignSortColumn,
        'campaignSortOrder': action.campaignSortOrder
      };

    default:
      return state;
  }
}


export default function campaignUniques(state = false, action) {
  switch (action.type) {

    case 'CAMPAIGN_UNIQUES_GET_RECEIVE':
      return action.campaignUniques || false;

    default:
      return state;
  }
}

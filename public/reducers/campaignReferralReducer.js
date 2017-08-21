export default function campaignReferrals(state = null, action) {
  switch (action.type) {

    case 'REFERRALS_GET_RECEIVE':
      return action.campaignReferrals || [];

    default:
      return state;
  }
}

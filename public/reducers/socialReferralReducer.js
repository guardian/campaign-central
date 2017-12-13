export default function socialReferrals(state = [], action) {
  switch (action.type) {

    case 'SOCIAL_REFERRALS_GET_RECEIVE':
      return action.socialReferrals || [];

    default:
      return state;
  }
}

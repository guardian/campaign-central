
const field = 'impressions';
const order = 'desc';

export default function campaignReferralOrderReducer(state = {field, order}, action) {
  switch (action.type) {

    case 'REFERRALS_ORDER':
      const toggle = state.order === 'asc' ? 'desc' : 'asc';
      return Object.assign({}, {
        field: action.field,
        order: state.field === action.field ? toggle : 'desc'
      });

    default:
      return state;
  }
}

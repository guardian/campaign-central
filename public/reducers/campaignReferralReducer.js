import CampaignReferral from "../components/CampaignAnalytics/Analytics/CampaignReferral";

function buildTree(referrals, ordering) {

  let len = referrals.length,
    tree = [],
    i;

  for (i = 0; i < len; i += 1) {

    const children =
      (referrals[i] && referrals[i].children && referrals[i].children.length > 0) ?
        buildTree(referrals[i].children, ordering) :
        [];

    tree.push({
      "name": referrals[i].sourceDescription,
      "id": i,
      "impressionCount": referrals[i].stats.impressionCount,
      "clickCount": referrals[i].stats.clickCount,
      "ctr": referrals[i].stats.ctr,
      "customComponent": CampaignReferral,
      "isOpen": false,
      "children": children
    });
  }

  return tree;
}

function sortTree(tree, ordering){
  // Sort children
  /*if (ordering) {
    const {order, field} = ordering;

    tree.sort((a, b) => {

      const compare = (numA, numB) => {
        const invert = order === 'asc' ? 1 : -1;
        if (numA < numB) {
          return -1 * invert;
        } else if (numA > numB) {
          return invert;
        } else {
          return 0;
        }
      };

      switch (field) {
        case 'impressions':
          return compare(a.impressionCount, b.impressionCount);
        case 'clicks':
          return compare(a.clickCount, b.clickCount);
        case 'ctr':
          return compare(a.ctr, b.ctr);
      }
    });
  }*/
  return tree;
}

const initialState = {
  tree: [],
  ordering: {
    field: 'impressions',
    order: 'desc'
  }
};

export default function campaignReferrals(state = initialState, action) {

  switch (action.type) {
    case 'REFERRALS_GET_RECEIVE': {
      // Deep copy, don't use Object.assign().
      const newState = JSON.parse(JSON.stringify(state));
      const tree = buildTree(action.campaignReferrals || []);
      newState.tree = tree;
      sortTree(newState.tree, newState.ordering);
      return newState;
    }
    case 'REFERRALS_TOGGLE_ORDER': {
      // Deep copy, don't use Object.assign().
      const newState = JSON.parse(JSON.stringify(state));
      newState.ordering.field = action.field || state.ordering.field;
      // If the field has not changed, toggle the order.
      const toggle = state.order === 'desc' ? 'asc' : 'desc';
      newState.ordering.order = state.ordering.field === action.ordering.field ? toggle : 'desc';
      sortTree(newState.tree, newState.ordering);
      return newState;
    }
    default:
      return state;
  }
}

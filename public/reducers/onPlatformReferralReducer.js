import CampaignReferral from "../components/CampaignAnalytics/Analytics/CampaignReferral";
import { cloneDeep } from 'lodash';

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

  tree.sort((a, b) => {

    const compare = (numA, numB) => {
      const invert = ordering.order === 'asc' ? 1 : -1;
      if (numA < numB) {
        return -1 * invert;
      } else if (numA > numB) {
        return invert;
      } else {
        return 0;
      }
    };

    switch (ordering.field) {
      case 'impressions':
        return compare(a.impressionCount, b.impressionCount);
      case 'clicks':
        return compare(a.clickCount, b.clickCount);
      case 'ctr':
        return compare(a.ctr, b.ctr);
    }
  });

  tree.forEach( node => {
    sortTree(node.children, ordering);
  });

  return tree;
}

const initialState = {
  tree: [],
  ordering: {
    field: 'impressions',
    order: 'desc'
  }
};

export default function onPlatformReferrals(state = initialState, action) {

  switch (action.type) {
    case 'PLATFORM_REFERRALS_GET_RECEIVE': {
      // Deep copy, don't use Object.assign().
      const newState = cloneDeep(state);
      const tree = buildTree(action.onPlatformReferrals || []);
      newState.tree = tree;
      sortTree(newState.tree, newState.ordering);
      return newState;
    }
    case 'REFERRALS_TOGGLE_ORDER': {
      // Deep copy, don't use Object.assign().
      const newState = cloneDeep(state);
      newState.ordering.field = action.field || state.ordering.field;
      // If the field has not changed, toggle the order.
      const toggle = state.ordering.order === 'desc' ? 'asc' : 'desc';
      newState.ordering.order = state.ordering.field === action.field ? toggle : 'desc';
      sortTree(newState.tree, newState.ordering);
      return newState;
    }
    case 'REFERRALS_TOGGLE_NODE': {
      // Deep copy, don't use Object.assign(). This appears to perform no modification,
      // but this copy is necessary. The InfinityMenu has already updated 'state', but those changes
      // will not propagate to the redux store, causing any props mapped from redux state to lose updates.
      //
      // FAQ: Why do this at all, why not just leave the InfinityMenu alone, given that it maintains it's own state?
      // We pass the tree state through the redux store, and the parent's props, because this reducer is then
      // given the opportunity to reorder children.
      return cloneDeep(state);
    }
    default:
      return state;
  }
}


export default function campaignPageViews(state = false, action) {
  switch (action.type) {

    case 'CAMPAIGN_PAGE_VIEWS_GET_RECEIVE':
      return action.campaignPageViews || false;

    default:
      return state;
  }
}

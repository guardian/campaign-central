
export default function campaignCtaStats(state = false, action) {
  switch (action.type) {

    case 'CAMPAIGN_CTA_STATS_GET_RECEIVE':
      return action.campaignCtaStats || false;

    default:
      return state;
  }
}

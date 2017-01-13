
export default function campaignQualifiedReport(state = false, action) {
  switch (action.type) {

    case 'CAMPAIGN_QUALIFIED_REPORT_GET_RECEIVE':
      return action.campaignQualifiedReport || false;

    default:
      return state;
  }
}

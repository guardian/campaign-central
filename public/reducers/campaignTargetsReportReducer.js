
export default function campaignTargetsReport(state = false, action) {
  switch (action.type) {

    case 'CAMPAIGN_TARGETS_REPORT_GET_RECEIVE':
      return action.campaignTargetsReport || false;

    default:
      return state;
  }
}

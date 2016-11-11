
export default function overallAnalyticsSummary(state = false, action) {
  switch (action.type) {

    case 'OVERALL_ANALYTICS_SUMMARY_GET_RECEIVE':
      return action.overallAnalyticsSummary || false;

    default:
      return state;
  }
}

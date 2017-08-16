
export default function latestAnalytics(state = false, action) {
  switch (action.type) {

    case 'LATEST_ANALYTICS_GET_RECEIVE':
      return action.latestAnalytics || false;

    default:
      return state;
  }
}

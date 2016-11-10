export default function campaignTrafficDriverStats(state = null, action) {
  switch (action.type) {

    case 'TRAFFIC_DRIVER_STATS_GET_RECEIVE':
      return action.campaignTrafficDriverStats || [];

    default:
      return state;
  }
}

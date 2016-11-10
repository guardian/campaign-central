export default function campaignTrafficDriverStats(state = [], action) {
  switch (action.type) {

    case 'TRAFFIC_DRIVER_STATS_GET_RECEIVE':
      return action.campaignTrafficDriverStats || [];

    default:
      return state;
  }
}

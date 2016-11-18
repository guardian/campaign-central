export default function campaignTrafficDrivers(state = null, action) {
  switch (action.type) {

    case 'TRAFFIC_DRIVERS_GET_RECEIVE':
      return action.campaignTrafficDrivers || [];

    default:
      return state;
  }
}

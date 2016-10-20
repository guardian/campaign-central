export default function campaignTrafficDrivers(state = [], action) {
  switch (action.type) {

    case 'TRAFFIC_DRIVERS_GET_RECEIVE':
      return action.campaignTrafficDrivers || [];

    default:
      return state;
  }
}

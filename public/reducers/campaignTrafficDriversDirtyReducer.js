export default function campaignTrafficDriversDirty(state = false, action) {
  switch (action.type) {

    case 'TRAFFIC_DRIVERS_GET_RECEIVE':
      return false;

    case 'CAMPAIGN_DRIVER_ACCEPT_RECEIVE':
      return true;

    default:
      return state;
  }
}

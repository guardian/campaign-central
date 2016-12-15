export default function campaignTrafficDriverSuggestions(state = null, action) {
  switch (action.type) {

    case 'TRAFFIC_DRIVER_SUGGESTIONS_GET_RECEIVE':
      return action.campaignTrafficDriverSuggestions || {};

    default:
      return state;
  }
}

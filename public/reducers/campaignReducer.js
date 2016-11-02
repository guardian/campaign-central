
export default function campaign(state = null, action) {
  switch (action.type) {

    case 'CAMPAIGN_GET_RECEIVE':
      return action.campaign || false;

    case 'CAMPAIGN_UPDATE_REQUEST':
      return action.campaign;

    default:
      return state;
  }
}

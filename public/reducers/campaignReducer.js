
export default function campaign(state = null, action) {
  switch (action.type) {

    case 'CAMPAIGN_GET_RECIEVE':
      return action.campaign || false;

    case 'CAMPAIGN_UPDATE_REQUEST':
      return action.campaign;

    default:
      return state;
  }
}

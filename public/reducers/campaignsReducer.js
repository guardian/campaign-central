
export default function campaigns(state = [], action) {
  switch (action.type) {

    case 'CAMPAIGNS_GET_RECIEVE':
      return action.campaigns || [];

    default:
      return state;
  }
}

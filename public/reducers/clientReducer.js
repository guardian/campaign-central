
export default function client(state = null, action) {
  switch (action.type) {

    case 'CLIENT_GET_RECEIVE':
      return action.client;

    case 'CLIENT_UPDATE_REQUEST':
      return action.client;

    default:
      return state;
  }
}

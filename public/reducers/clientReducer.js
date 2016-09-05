
export default function client(state = null, action) {
  switch (action.type) {

    case 'CLIENT_GET_RECIEVE':
      return action.client || [];

    default:
      return state;
  }
}

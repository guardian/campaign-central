
export default function clients(state = [], action) {
  switch (action.type) {

    case 'CLIENTS_GET_RECIEVE':
      return action.clients || [];

    default:
      return state;
  }
}

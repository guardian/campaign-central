
export default function clients(state = [], action) {
  switch (action.type) {

    case 'CLIENTS_GET_RECEIVE':
      return action.clients || [];

    default:
      return state;
  }
}

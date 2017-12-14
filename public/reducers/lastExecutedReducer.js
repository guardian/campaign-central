
export default function lastExecuted(state = null, action) {
  switch (action.type) {

    case 'LAST_EXECUTED_GET_RECEIVE':
      return action.lastExecuted || false;

    default:
      return state;
  }
}

export default function territory(state = 'global', action) {
  switch (action.type) {
    case 'SET_TERRITORY':
      return action.territory || state

    default:
      return state;
  }
}


export default function campaignNotes(state = [], action) {
  switch (action.type) {

    case 'NOTES_GET_RECIEVE':
      return action.campaignNotes || [];

    default:
      return state;
  }
}

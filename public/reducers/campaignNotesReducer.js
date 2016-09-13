
export default function campaignNotes(state = [], action) {
  switch (action.type) {

    case 'NOTES_GET_RECIEVE':
      return action.campaignNotes || [];

    case 'NOTE_CREATE_RECIEVE':
      return state.concat([action.note]);

    default:
      return state;
  }
}

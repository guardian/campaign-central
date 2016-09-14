
export default function campaignNotes(state = [], action) {
  switch (action.type) {

    case 'NOTES_GET_RECIEVE':
      return action.campaignNotes || [];

    case 'NOTE_CREATE_RECIEVE':
      return state.concat([action.note]);

    case 'NOTE_UPDATE_RECIEVE':
      return state.map(note => {
        if (note.created === action.note.created) {
          return action.note;
        }
        return note;
      });

    default:
      return state;
  }
}

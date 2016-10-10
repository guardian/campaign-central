
export default function campaignContent(state = [], action) {
  switch (action.type) {

    case 'CONTENT_GET_RECIEVE':
      return action.campaignContent || [];

    default:
      return state;
  }
}

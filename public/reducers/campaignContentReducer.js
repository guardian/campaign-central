
export default function campaignContent(state = [], action) {
  switch (action.type) {

    case 'CONTENT_GET_RECEIVE':
      return action.campaignContent || [];

    default:
      return state;
  }
}

import React, {Component, PropTypes} from 'react';
import CampaignNotesListItem from './CampaignNotesListItem';

class CampaignNotesList extends Component {

  static defaultProps = {
    campaignNotes: []
  };

  static propTypes = {
    campaignNotes: PropTypes.array,
    campaignId: PropTypes.string
  };

  render() {
    if (!this.props.campaignNotes.length) {
      return (
        <div>This campaign does not have any notes</div>
      );
    }

    return (
      <div>
        {this.props.campaignNotes.map((note) => <CampaignNotesListItem key={note.created} noteContent={note.content}/> )}
      </div>
    );
  }
}

export default CampaignNotesList;

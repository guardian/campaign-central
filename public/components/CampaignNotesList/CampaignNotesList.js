import React, {Component, PropTypes} from 'react';
import CampaignNotesAdd from '../CampaignNotes/CampaignNotesAdd';

class CampaignNotesList extends Component {

  static defaultProps = {
    campaignNotes: [],
    onSave: PropTypes.func.isRequired
  };

  static propTypes = {
    campaignNotes: PropTypes.array.isRequired,
    campaignId: PropTypes.string.isRequired
  };

  render() {
    if (!this.props.campaignNotes.length) {
      return (
        <div>This campaign does not have any notes</div>
      );
    }

    return (
      <div>
        {this.props.campaignNotes.map((note) => <CampaignNotesAdd key={note.created} id={this.props.campaignId} content={note.content} created={note.created} onSave={this.props.onSave}/> )}
      </div>
    );
  }
}

export default CampaignNotesList;

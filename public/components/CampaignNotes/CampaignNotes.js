import React, {Component, PropTypes} from 'react';
import CampaignNotesList from '../CampaignNotesList/CampaignNotesList';
import CampaignNotesAdd from './CampaignNotesAdd';

class CampaignNotes extends Component {

  componentWillMount() {
    this.props.campaignNoteActions.getCampaignNotes(this.props.campaign.id);
  }

  saveNote = () => {
    this.props.campaignNotesAddActions.createNote(this.props.id, { content: this.state.message });
    window.removeEventListener('click', this.disableAdding, true);

    this.setState({
      adding: false
    });
  }


  render() {

    return (
      <div className="campaign-info campaign-box-section">
        <div className="campaign-box-section__header">Notes</div>
        <div className="campaign-box-section__body">
          <CampaignNotesList campaignNotes={this.props.campaignNotes} campaignId={this.props.campaign.id} onSave={this.props.campaignNoteActions.updateNote}/>
          <CampaignNotesAdd id={this.props.campaign.id} onSave={this.props.campaignNoteActions.createNote}/>
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaignNotes from '../../actions/CampaignActions/getCampaignNotes';
import * as createNote from '../../actions/CampaignActions/createCampaignNote';
import * as updateNote from '../../actions/CampaignActions/updateCampaignNote';

function mapStateToProps(state) {
  return {
    campaignNotes: state.campaignNotes,
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignNoteActions: bindActionCreators(Object.assign({}, getCampaignNotes, createNote, updateNote), dispatch),
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignNotes);

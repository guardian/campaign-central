import React, {Component, PropTypes} from 'react';
import CampaignNotesList from '../CampaignNotesList/CampaignNotesList';

class CampaignNotes extends Component {

  componentWillMount() {
    this.props.campaignNoteActions.getCampaignNotes(this.props.campaign.id);
  }

  render() {

    return (
      <div className="campaign-info campaign-box-section">
        <div className="campaign-box-section__header">Notes</div>
        <CampaignNotesList campaignNotes={this.props.campaignNotes}/>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaignNotes from '../../actions/CampaignActions/getCampaignNotes';

function mapStateToProps(state) {
  return {
    campaignNotes: state.campaignNotes
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignNoteActions: bindActionCreators(Object.assign({}, getCampaignNotes), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignNotes);

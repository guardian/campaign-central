import React, {Component, PropTypes} from 'react';

class CampaignNotes extends Component {

  componentWillMount() {
    this.props.campaignNoteActions.getCampaignNotes(this.props.campaign.id);
  }

  render() {


    if (!this.props.campaignNotes) {
        return <div>Loading notes...</div>;
    }

    if (!this.props.campaignNotes.length) {
      return (
        <div className="notes">
          This campaign does not have any notes
        </div>
      );
    }

    return (
      <div className="campaign-info campaign-box-section">
        <div className="campaign-box-section__header">Notes</div>
        <div className="campaign-box-section__body">
          {this.props.campaignNotes.map((n) => <div key={n.id}>{n.content}</div>)}
        </div>
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

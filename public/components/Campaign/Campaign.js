import React, { PropTypes } from 'react';
import CampaignInformation from '../CampaignInformation/CampaignInformation';

class Campaign extends React.Component {

  componentWillMount() {
    this.props.campaignActions.getCampaign(this.props.params.id);
  }

  render () {
    if (!this.props.campaign) {
      return <div>Loading... </div>;
    }

    return (
      <div className="campaign">
        <h2>{this.props.campaign.name}</h2>
        <CampaignInformation campaign={this.props.campaign}/>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaign from '../../actions/CampaignActions/getCampaign';

function mapStateToProps(state) {
  return {
    campaign: state.campaign
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({}, getCampaign), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaign);

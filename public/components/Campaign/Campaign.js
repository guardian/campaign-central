import React, { PropTypes } from 'react';
import CampaignEdit from '../CampaignInformationEdit/CampaignEdit';
import CampaignAssets from '../CampaignInformationEdit/CampaignAssets';
import CampaignAnalytics from '../CampaignAnalytics/CampaignAnalytics';
import CampaignTrafficDrivers from '../CampaignInformationEdit/CampaignTrafficDrivers';

class Campaign extends React.Component {

  componentWillMount() {
    this.props.campaignActions.getCampaign(this.props.params.id);
  }

  render () {
    const campaign = this.props.campaign && this.props.params.id === this.props.campaign.id ? this.props.campaign : undefined;

    if (!campaign) {
      return <div>Loading... </div>;
    }

    return (
      <div className="campaign">
        <h2>{campaign.name}</h2>
        <div className="campaign__row">
          <CampaignEdit campaign={campaign} updateCampaign={this.props.campaignActions.updateCampaign} saveCampaign={this.props.campaignActions.saveCampaign}/>
          <CampaignAssets campaign={campaign}
                          getCampaign={this.props.campaignActions.getCampaign}
                          getCampaignContent={this.props.campaignActions.getCampaignContent} />
          <CampaignTrafficDrivers campaign={campaign} />
          <CampaignAnalytics campaign={campaign} />
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaign from '../../actions/CampaignActions/getCampaign';
import * as updateCampaign from '../../actions/CampaignActions/updateCampaign';
import * as saveCampaign from '../../actions/CampaignActions/saveCampaign';
import * as getCampaignContent from '../../actions/CampaignActions/getCampaignContent';

function mapStateToProps(state) {
  return {
    campaign: state.campaign
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({}, getCampaign, updateCampaign, saveCampaign, getCampaignContent), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaign);

import React, { PropTypes } from 'react';
import {Link} from 'react-router';
import CampaignEdit from '../CampaignInformationEdit/CampaignEdit';
import CampaignAssets from '../CampaignInformationEdit/CampaignAssets';
import CampaignAnalytics from '../CampaignAnalytics/CampaignAnalytics';
import CampaignTrafficDriversAndSuggestions from '../CampaignInformationEdit/CampaignTrafficDriversAndSuggestions';

class Campaign extends React.Component {

  componentWillMount() {
    this.props.campaignActions.getCampaign(this.props.params.id);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign && (!this.props.campaign || nextProps.campaign.id !== this.props.campaign.id)) {
      this.props.campaignAnalyticsActions.clearCampaignAnalytics();
      if (this.isAnalysisAvailable(nextProps.campaign)) {
        this.props.campaignAnalyticsActions.getCampaignAnalytics(nextProps.campaign.id)
      }
    }
  }

  isAnalysisAvailable(campaign) {
    const analysableStatus = campaign.status === 'live' || campaign.status === 'dead';
    return (analysableStatus && campaign.startDate && campaign.pathPrefix );
  }

  deleteCampaign = () => {
    this.props.campaignActions.deleteCampaign(this.props.campaign.id);
  }

  render () {
    const campaign = this.props.campaign && this.props.params.id === this.props.campaign.id ? this.props.campaign : undefined;

    if (!campaign) {
      return <div>Loading... </div>;
    }

    return (
      <div className="campaign">
        <h2>{campaign.name}</h2>
        <Link className="campaign-box__header__delete-campaign-button" to={"/campaigns"} onClick={this.deleteCampaign} >
          Delete Campaign <i className="i-delete"/>
        </Link>
        <div className="campaign__row">
          <CampaignEdit campaign={campaign} updateCampaign={this.props.campaignActions.updateCampaign} saveCampaign={this.props.campaignActions.saveCampaign}/>
          <CampaignAssets campaign={campaign}
                          getCampaign={this.props.campaignActions.getCampaign}
                          getCampaignContent={this.props.campaignActions.getCampaignContent} />
          <CampaignTrafficDriversAndSuggestions campaign={campaign} />
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
import * as deleteCampaign from '../../actions/CampaignActions/deleteCampaign';
import * as getCampaignContent from '../../actions/CampaignActions/getCampaignContent';
import * as getCampaignAnalytics from '../../actions/CampaignActions/getCampaignAnalytics';
import * as clearCampaignAnalytics from '../../actions/CampaignActions/clearCampaignAnalytics';

function mapStateToProps(state) {
  return {
    campaign: state.campaign
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({}, getCampaign, updateCampaign, saveCampaign, deleteCampaign, getCampaignContent), dispatch),
    campaignAnalyticsActions: bindActionCreators(Object.assign({}, getCampaignAnalytics, clearCampaignAnalytics), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaign);

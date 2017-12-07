import React from 'react';
import {Link} from 'react-router';
import CampaignEdit from '../CampaignInformationEdit/CampaignEdit';
import CampaignAssets from '../CampaignInformationEdit/CampaignAssets';
import CampaignUniquesChart from '../CampaignAnalytics/Analytics/CampaignUniquesChart';
import CampaignReferrals from '../CampaignAnalytics/Analytics/CampaignReferrals';
import CampaignPerformanceOverview from '../CampaignPerformanceOverview/CampaignPerformanceOverview';
import CampaignPerformanceBreakdown from '../CampaignPerformanceBreakdown/CampaignPerformanceBreakdown';
import CampaignMediaEvents from '../CampaignMediaEvents/CampaignMediaEvents';

class Campaign extends React.Component {

  componentWillMount() {
    this.props.campaignActions.getCampaign(this.props.params.id);
  }

  componentWillReceiveProps(nextProps) {
    const isTerritoryDifferent = this.props.territory !== nextProps.territory;

    if (nextProps.campaign && (!this.props.campaign || nextProps.campaign.id !== this.props.campaign.id || isTerritoryDifferent)) {
      this.props.analyticsActions.clearCampaignAnalytics();
      if (this.isAnalysisAvailable(nextProps.campaign)) {
        this.props.analyticsActions.getCampaignPageViews(nextProps.campaign.id);
        this.props.analyticsActions.getCampaignUniques(nextProps.campaign.id, nextProps.territory);
        this.props.analyticsActions.getCampaignMediaEvents(nextProps.campaign.id);
        this.props.analyticsActions.getLatestAnalyticsForCampaign(nextProps.campaign.id, nextProps.territory);
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

  renderPercentageOfTarget(actual, target) {
    if (actual && target && target != 0) {
      return (
        Math.round(100*actual/target) +"% of target"
      );
    } else { return (null); }
  }

  renderReferralsIfGlobal(campaign) {
    if ("global" === this.props.territory) {
      return(<CampaignReferrals campaign={campaign} />);
    } else { return (null); }
  }

  renderMediaEventsIfGlobal(campaign) {
    if ("global" === this.props.territory) {
      return(<CampaignMediaEvents campaign={campaign} mediaEventsData={this.props.campaignMediaEvents} />);
    } else { return (null); }
  }

  renderCampaignUniquesChart() {
    if (!this.props.campaignUniques || !this.props.campaignUniques.length > 0) return null;

    return (
      <CampaignUniquesChart data={this.props.campaignUniques}/>
    );
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
          <CampaignPerformanceOverview campaign={campaign}
                        latestAnalyticsForCampaign={this.props.latestAnalyticsForCampaign} />

          <CampaignPerformanceBreakdown campaign={campaign}
                        latestAnalyticsForCampaign={this.props.latestAnalyticsForCampaign}
                        territory={this.props.territory} />

          <CampaignEdit campaign={campaign}
                        updateCampaign={this.props.campaignActions.updateCampaign}
                        saveCampaign={this.props.campaignActions.saveCampaign}/>
          {this.renderCampaignUniquesChart()}
          <CampaignAssets campaign={campaign}
                          getCampaign={this.props.campaignActions.getCampaign}
                          getCampaignContent={this.props.campaignActions.getCampaignContent} />
          {this.renderReferralsIfGlobal(campaign)}
          {this.renderMediaEventsIfGlobal(campaign)}
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
import * as getCampaignPageViews from '../../actions/CampaignActions/getCampaignPageViews';
import * as getCampaignUniques from '../../actions/CampaignActions/getCampaignUniques';
import * as clearCampaignAnalytics from '../../actions/CampaignActions/clearCampaignAnalytics';
import * as getLatestAnalyticsForCampaign from '../../actions/CampaignActions/getLatestAnalyticsForCampaign';
import * as getCampaignMediaEvents from '../../actions/CampaignActions/getCampaignMediaEvents';

function mapStateToProps(state) {
  return {
    campaign: state.campaign,
    latestAnalyticsForCampaign: state.latestAnalyticsForCampaign,
    campaignMediaEvents: state.campaignMediaEvents,
    territory: state.territory,
    campaignUniques: state.campaignUniques
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignActions: bindActionCreators(Object.assign({},
      getCampaign, updateCampaign, saveCampaign, deleteCampaign, getCampaignContent), dispatch),
    analyticsActions: bindActionCreators(Object.assign({},
      getLatestAnalyticsForCampaign, getCampaignMediaEvents, getCampaignPageViews, getCampaignUniques, clearCampaignAnalytics), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Campaign);

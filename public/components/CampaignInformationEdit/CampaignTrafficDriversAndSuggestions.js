import React, {PropTypes} from "react";
import CampaignTrafficDrivers from "./CampaignTrafficDrivers";
import CampaignTrafficDriverSuggestions from "./CampaignTrafficDriverSuggestions";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as getCampaignTrafficDrivers from "../../actions/CampaignActions/getCampaignTrafficDrivers";
import * as getCampaignTrafficDriverSuggestions from "../../actions/CampaignActions/getCampaignTrafficDriverSuggestions";
import * as acceptSuggestedCampaignTrafficDriver from "../../actions/CampaignActions/AcceptSuggestedCampaignTrafficDriver";
import * as rejectSuggestedCampaignTrafficDriver from "../../actions/CampaignActions/RejectSuggestedCampaignTrafficDriver";

class CampaignTrafficDriversAndSuggestions extends React.Component {

  componentWillMount() {
    this.props.campaignTrafficDriverActions.getCampaignTrafficDrivers(this.props.campaign.id);
    this.props.campaignTrafficDriverSuggestionActions.getCampaignTrafficDriverSuggestions(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    const campaignChanged = nextProps.campaign.id !== this.props.campaign.id;
    const driversNeedRefresh = nextProps.campaignTrafficDriversDirty && !this.props.campaignTrafficDriversDirty;
    const suggestionsNeedRefresh = nextProps.campaignTrafficDriverSuggestionsDirty && !this.props.campaignTrafficDriverSuggestionsDirty;

    if (campaignChanged || driversNeedRefresh) {
      this.props.campaignTrafficDriverActions.getCampaignTrafficDrivers(nextProps.campaign.id);
    }

    if (campaignChanged || driversNeedRefresh || suggestionsNeedRefresh) {
      this.props.campaignTrafficDriverSuggestionActions.getCampaignTrafficDriverSuggestions(nextProps.campaign.id);
    }
  }

  acceptSuggestion = (trafficDriverId) => {
    this.props.campaignTrafficDriverSuggestionActions.acceptSuggestedCampaignTrafficDriver(this.props.campaign.id, trafficDriverId);
  };

  rejectSuggestion = (trafficDriverId) => {
    this.props.campaignTrafficDriverSuggestionActions.rejectSuggestedCampaignTrafficDriver(this.props.campaign.id, trafficDriverId);
  };

  render() {
    return (
      <div className="campaign-info campaign-box">
        <a name="driver-summary"/>
        <div className="campaign-box__header">Traffic Drivers</div>
        <div className="campaign-box__body">
          <CampaignTrafficDrivers campaignTrafficDrivers={this.props.campaignTrafficDrivers}/>
          <CampaignTrafficDriverSuggestions campaignId={this.props.campaign.id}
                                            campaignTrafficDriverSuggestions={this.props.campaignTrafficDriverSuggestions}
                                            acceptSuggestion={(trafficDriverId) => this.acceptSuggestion(trafficDriverId)}
                                            rejectSuggestion={(trafficDriverId) => this.rejectSuggestion(trafficDriverId)}/>
        </div>
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    campaignTrafficDrivers: state.campaignTrafficDrivers,
    campaignTrafficDriverSuggestions: state.campaignTrafficDriverSuggestions,
    campaignTrafficDriversDirty: state.campaignTrafficDriversDirty,
    campaignTrafficDriverSuggestionsDirty: state.campaignTrafficDriverSuggestionsDirty
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignTrafficDriverActions:           bindActionCreators(Object.assign({}, getCampaignTrafficDrivers), dispatch),
    campaignTrafficDriverSuggestionActions: bindActionCreators(Object.assign({},
      getCampaignTrafficDriverSuggestions,
      acceptSuggestedCampaignTrafficDriver,
      rejectSuggestedCampaignTrafficDriver
    ), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignTrafficDriversAndSuggestions);

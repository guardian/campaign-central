import React, {PropTypes} from "react";
import ProgressSpinner from "../utils/ProgressSpinner";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as getCampaignTrafficDrivers from "../../actions/CampaignActions/getCampaignTrafficDrivers";

class CampaignTrafficDrivers extends React.Component {

  componentWillMount() {
    this.props.campaignTrafficDriverActions.getCampaignTrafficDrivers(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign.id !== this.props.campaign.id) {
      this.props.campaignTrafficDriverActions.getCampaignTrafficDrivers(nextProps.campaign.id);
    }
  }

  renderLineItemLink = (url) => {
    return (
      <a key={url} href={url} target="_blank" title="DFP line item">
        <i className="i-dfp"/>
      </a>
    );
  };

  renderTrafficDriverGroup = (group) => {
    return (
      <div key={group.groupName} className="campaign-driver-list__item">
        <div className="campaign-driver-list__row">
          <div className="campaign-driver-list__type">{group.groupName}</div>
          <div className="campaign-driver-list__date">{group.startDate}</div>
          <div className="campaign-driver-list__date">{group.endDate}</div>
          <div className="campaign-driver-list__stat">
            <a href="TODO">{group.summaryStats.impressions}</a>
          </div>
          <div className="campaign-driver-list__stat">
            <a href="TODO">{group.summaryStats.clicks}</a>
          </div>
          <div className="campaign-driver-list__stat">
            <a href="TODO">{group.summaryStats.ctr.toFixed(2)}%</a>
          </div>
          <div className="campaign-driver-list__links">{group.trafficDriverUrls.map( this.renderLineItemLink )}</div>
        </div>
      </div>
    );
  };

  renderTrafficDriverGroups = () => {

    if(!this.props.campaignTrafficDrivers) {
      return (<ProgressSpinner />);
    }

    if(this.props.campaignTrafficDrivers.length > 0) {
      return (
        <div className="campaign-driver-list campaign-assets__field__value">
          <div className="campaign-driver-list__row">
            <div className="campaign-driver-list__type--header">Type</div>
            <div className="campaign-driver-list__date--header">Start</div>
            <div className="campaign-driver-list__date--header">End</div>
            <div className="campaign-driver-list__stat--header">Impressions</div>
            <div className="campaign-driver-list__stat--header">Clicks</div>
            <div className="campaign-driver-list__stat--header">CTR</div>
            <div className="campaign-driver-list__links--header">Line items</div>
          </div>
          {this.props.campaignTrafficDrivers.map( this.renderTrafficDriverGroup ) }
        </div>
      );
    }

    return (
      <span className="campaign-assets__field__value">No traffic drivers have been created yet</span>
    )
  };

  render() {
    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header">Traffic Drivers</div>
        <div className="campaign-box__body">
          {this.renderTrafficDriverGroups()}
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS

function mapStateToProps(state) {
  return {
    campaignTrafficDrivers: state.campaignTrafficDrivers,
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignTrafficDriverActions: bindActionCreators(Object.assign({}, getCampaignTrafficDrivers), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignTrafficDrivers);

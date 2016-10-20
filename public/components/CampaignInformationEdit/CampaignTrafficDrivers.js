import React, {PropTypes} from "react";
import ProgressSpinner from '../utils/ProgressSpinner';

class CampaignTrafficDrivers extends React.Component {

  componentWillMount() {
    this.props.campaignTrafficDriverActions.getCampaignTrafficDrivers(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign.id !== this.props.campaign.id) {
      this.props.campaignTrafficDriverActions.getCampaignTrafficDrivers(nextProps.campaign.id);
    }
  }

  renderTrafficDriver = (driver) => {
    return (
      <div key={driver.id} className="campaign-driver-list__item">
        <div className="campaign-driver-list__row">
          <div className="campaign-driver-list__type">
            <a href={driver.url}>{driver.driverType}</a>
          </div>
          <div className="campaign-driver-list__status">{driver.status}</div>
          <div className="campaign-driver-list__date">{driver.startDate}</div>
          <div className="campaign-driver-list__date">{driver.endDate}</div>
          <div className="campaign-driver-list__stat">
            <a href="TODO">{driver.impressionsDelivered}</a>
          </div>
          <div className="campaign-driver-list__stat">
            <a href="TODO">{driver.clicksDelivered}</a>
          </div>
          <div className="campaign-driver-list__stat">
            <a href="TODO">{driver.ctrDelivered.toFixed(3)}</a>
          </div>
        </div>
      </div>
    );
  };

  renderTrafficDrivers = () => {

    if(!this.props.campaignTrafficDrivers) {
      return (<ProgressSpinner />);
    }

    if(this.props.campaignTrafficDrivers.length > 0) {
      return (
        <div className="campaign-driver-list campaign-assets__field__value">
          <div className="campaign-driver-list__row">
            <div className="campaign-driver-list__type--header">Type</div>
            <div className="campaign-driver-list__status--header">Status</div>
            <div className="campaign-driver-list__date--header">Start</div>
            <div className="campaign-driver-list__date--header">End</div>
            <div className="campaign-driver-list__stat--header">Impressions</div>
            <div className="campaign-driver-list__stat--header">Clicks</div>
            <div className="campaign-driver-list__stat--header">CTR</div>
          </div>
          {this.props.campaignTrafficDrivers.map( this.renderTrafficDriver ) }
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
          {this.renderTrafficDrivers()}
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaignTrafficDrivers from '../../actions/CampaignActions/getCampaignTrafficDrivers';

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

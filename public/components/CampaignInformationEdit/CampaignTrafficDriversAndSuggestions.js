import React, {PropTypes} from "react";
import CampaignTrafficDrivers from "./CampaignTrafficDrivers";
import CampaignTrafficDriverSuggestions from "./CampaignTrafficDriverSuggestions";

class CampaignTrafficDriversAndSuggestions extends React.Component {

  render() {
    return (
      <div className="campaign-info campaign-box">
        <a name="driver-summary"/>
        <div className="campaign-box__header">Traffic Drivers</div>
        <div className="campaign-box__body">
          <CampaignTrafficDrivers campaign={this.props.campaign}/>
          <CampaignTrafficDriverSuggestions campaign={this.props.campaign}/>
        </div>
      </div>
    );
  }
}

export default CampaignTrafficDriversAndSuggestions;

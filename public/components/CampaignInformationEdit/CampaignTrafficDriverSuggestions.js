import React, {PropTypes} from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";

class CampaignTrafficDriverSuggestions extends React.Component {

  renderSuggestion = (trafficDriver) => {
    return (
      <div key={trafficDriver.id} className="campaign-suggestion-list__row">
        <span><i className="i-dfp"/></span>
        <span><a href={trafficDriver.url} target="_blank">{trafficDriver.name} ({trafficDriver.id})</a></span>
        <span className="campaign-suggestion-list__button"><button  onClick={() => this.props.acceptSuggestion(trafficDriver.id)}>Yes</button></span>
        <span className="campaign-suggestion-list__button"><button onClick={() => this.props.rejectSuggestion(trafficDriver.id)}>No</button></span>
      </div>
    );
  };

  renderSuggestionGroup = (group) => {
    var suggestions = this.props.campaignTrafficDriverSuggestions[group];
    return (
      <div key={group} className="campaign-suggestion-list__row">
        <div>{group}:</div>
        {suggestions.map( this.renderSuggestion ) }
      </div>
    );
  };

  render() {

    if(!this.props.campaignTrafficDriverSuggestions) {
      return (<div></div>);
    }

    var groups = Object.keys(this.props.campaignTrafficDriverSuggestions);

    if(groups.length > 0) {
      return (
        <div>
          <div className="campaign-suggestion-list__header">Are any of these traffic drivers for this campaign?</div>
          {groups.map(this.renderSuggestionGroup) }
        </div>
      );
    }

    return (<div></div>);
  };
}

export default CampaignTrafficDriverSuggestions;

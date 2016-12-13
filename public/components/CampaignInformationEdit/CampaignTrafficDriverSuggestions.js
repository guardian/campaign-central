import React, {PropTypes} from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as getCampaignTrafficDriverSuggestions from "../../actions/CampaignActions/getCampaignTrafficDriverSuggestions";

class CampaignTrafficDriverSuggestions extends React.Component {

  componentWillMount() {
    this.props.campaignTrafficDriverSuggestionActions.getCampaignTrafficDriverSuggestions(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign.id !== this.props.campaign.id) {
      this.props.campaignTrafficDriverSuggestionActions.getCampaignTrafficDriverSuggestions(nextProps.campaign.id);
    }
  }

  linkLineItemToCampaign = () => {
    console.log("* Link line item x to campaign y");
    alert("Does nothing yet!")
  };

  ignoreLineItemForThisCampaign = () => {
    console.log("* Ignore line item x for campaign y");
    alert("Does nothing yet!")
  };

  renderLineItem = (lineItem) => {
    return (
      <div key={lineItem.id} className="campaign-suggestion-list__row">
        <span><i className="i-dfp"/></span>
        <span><a href={lineItem.url}>{lineItem.name} ({lineItem.id})</a></span>
        <span className="campaign-suggestion-list__button"><button onClick={this.linkLineItemToCampaign}>Yes</button></span>
        <span className="campaign-suggestion-list__button"><button onClick={this.ignoreLineItemForThisCampaign}>No</button></span>
      </div>
    );
  };

  renderSuggestionGroup = (group) => {
    var lineItems = this.props.campaignTrafficDriverSuggestions[group];
    return (
      <div key={group} className="campaign-suggestion-list__row">
        <div>{group}:</div>
        {lineItems.map( this.renderLineItem ) }
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
          <div className="campaign-suggestion-list__header">Should any of these be linked to this campaign?</div>
          {groups.map(this.renderSuggestionGroup) }
        </div>
      );
    }

    return (<div></div>);
  };
}

//REDUX CONNECTIONS

function mapStateToProps(state) {
  return {
    campaignTrafficDriverSuggestions: state.campaignTrafficDriverSuggestions
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignTrafficDriverSuggestionActions: bindActionCreators(Object.assign({}, getCampaignTrafficDriverSuggestions), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignTrafficDriverSuggestions);

import React, { PropTypes } from 'react'
import CampaignDailyTrafficChart from './Analytics/CampaignDailyTrafficChart'
import CampaignPagesCumulativeTrafficChart from './Analytics/CampaignPagesCumulativeTrafficChart'

class CampaignAnalytics extends React.Component {

  componentWillMount() {
    this.props.campaignAnalyticsActions.getCampaignAnalytics(this.props.campaign.id);
  }

  render () {
    if (!this.props.campaignAnalytics) {
      return <div className="campaign-info__body">Loading... </div>;
    }

    return (
      <div className="campaign-info__body">
        
        Here's where all the charts go... known paths<br/>
        <ul>
          {this.props.campaignAnalytics.seenPaths.map((p) => <li key={p}>{p}</li>)}
        </ul>
        <CampaignDailyTrafficChart pageCountStats={this.props.campaignAnalytics.pageCountStats} />
        <CampaignPagesCumulativeTrafficChart pageCountStats={this.props.campaignAnalytics.pageCountStats} paths={this.props.campaignAnalytics.seenPaths}/>
      </div>
    );
  }
}


//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaignAnalytics from '../../actions/CampaignActions/getCampaignAnalytics';

function mapStateToProps(state) {
  return {
    campaignAnalytics: state.campaignAnalytics
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignAnalyticsActions: bindActionCreators(Object.assign({}, getCampaignAnalytics), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignAnalytics);
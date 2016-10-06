import React, { PropTypes } from 'react'
import CampaignPerformanceSummary from './Analytics/CampaignPerformanceSummary'
import CampaignDailyTrafficChart from './Analytics/CampaignDailyTrafficChart'
import CampaignPagesCumulativeTrafficChart from './Analytics/CampaignPagesCumulativeTrafficChart'
import ContentTrafficChart from './Analytics/ContentTrafficChart'

class CampaignAnalytics extends React.Component {

  componentWillMount() {
    if (this.isAnalysisAvailable()) {
      this.props.campaignAnalyticsActions.getCampaignAnalytics(this.props.campaign.id);
    }
  }

  isAnalysisAvailable() {
    return (this.props.campaign.status === 'live' && this.props.campaign.startDate && this.props.campaign.pathPrefix );
  }

  getLatestCounts() {
    if(this.props.campaignAnalytics) {
      return this.props.campaignAnalytics.pageCountStats[this.props.campaignAnalytics.pageCountStats.length - 1];
    }
    
    return undefined;
  }

  render () {
    if (!this.isAnalysisAvailable()) {
      return null;
    }

    if (!this.props.campaignAnalytics) {
      return <div className="campaign-info__body">Loading... </div>;
    }

    return (
      <div className="campaign-info__body">
        
        <CampaignPerformanceSummary campaign={this.props.campaign} paths={this.props.campaignAnalytics.seenPaths} latestCounts={this.getLatestCounts()} />
        <CampaignDailyTrafficChart pageCountStats={this.props.campaignAnalytics.pageCountStats} />
        <CampaignPagesCumulativeTrafficChart pageCountStats={this.props.campaignAnalytics.pageCountStats} paths={this.props.campaignAnalytics.seenPaths}/>
        {this.props.campaignAnalytics.seenPaths.map((p) =>
          <ContentTrafficChart key={p} pageCountStats={this.props.campaignAnalytics.pageCountStats} path={p} />
        )}
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
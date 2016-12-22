import React, {PropTypes} from "react";
import CampaignPerformanceSummary from "./Analytics/CampaignPerformanceSummary";
import CampaignDailyTrafficChart from "./Analytics/CampaignDailyTrafficChart";
import CampaignPagesCumulativeTrafficChart from "./Analytics/CampaignPagesCumulativeTrafficChart";
import ContentTrafficChart from "./Analytics/ContentTrafficChart";
import CampaignTrafficDriverStatsChart from "./Analytics/CampaignTrafficDriverStatsChart";

class CampaignAnalytics extends React.Component {

  isAnalysisAvailable(campaign) {
    const analysableStatus = campaign.status === 'live' || campaign.status === 'dead';
    return (analysableStatus && campaign.startDate && campaign.pathPrefix );
  }

  getLatestCounts() {
    if(this.props.campaignPageViews) {
      return this.props.campaignPageViews.pageCountStats[this.props.campaignPageViews.pageCountStats.length - 1];
    }

    return undefined;
  }

  render () {
    if (!this.isAnalysisAvailable(this.props.campaign)) {
      return null;
    }

    if (!this.props.campaignPageViews) {
      return <div className="campaign-info__body">Loading... </div>;
    }

    return (
      <div>
        <div className="campaign-info__body">

          <CampaignPerformanceSummary campaign={this.props.campaign} paths={this.props.campaignPageViews.seenPaths}
                                      latestCounts={this.getLatestCounts()}/>
          <CampaignDailyTrafficChart pageCountStats={this.props.campaignPageViews.pageCountStats}/>
          <CampaignPagesCumulativeTrafficChart pageCountStats={this.props.campaignPageViews.pageCountStats}
                                               paths={this.props.campaignPageViews.seenPaths}/>
          {this.props.campaignPageViews.seenPaths.map((p) =>
            <ContentTrafficChart key={p} pageCountStats={this.props.campaignPageViews.pageCountStats} path={p}/>
          )}
        </div>
        <div className="campaign-info__body">
          <CampaignTrafficDriverStatsChart campaign={this.props.campaign}/>
        </div>
      </div>
    );
  }
}


//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';

function mapStateToProps(state) {
  return {
    campaignPageViews: state.campaignPageViews
  };
}

export default connect(mapStateToProps)(CampaignAnalytics);

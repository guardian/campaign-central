import React, { PropTypes } from 'react';
import BigCardMetric from './BigCardMetric';

export default class CampaignPerformanceOverview extends React.Component {

  renderPercentageOfTarget(actual, target) {
    if (actual && target && target != 0) {
      return (
        "(" + Math.round(100*actual/target) +"% of target)"
      );
    } else { return (null); }
  }

  render () {

    const totalPageviews = this.props.latestAnalyticsForCampaign.pageviews;
    const pageviewsPerDevice = this.props.latestAnalyticsForCampaign.pageviewsByDevice;

    const totalUniques = this.props.latestAnalyticsForCampaign.uniques;
    const uniquesPerDevice = this.props.latestAnalyticsForCampaign.uniquesByDevice;
    const uniquesTarget = this.props.campaign.targets && this.props.campaign.targets.uniques;

    const medianAttentionTime = this.props.latestAnalyticsForCampaign.medianAttentionTimeSeconds;
    const medianPerDevice = this.props.latestAnalyticsForCampaign.medianAttentionTimeByDevice || {};

    const averageDwellTimePerPathSeconds = this.props.latestAnalyticsForCampaign.averageDwellTimePerPathSeconds || {};
    const weightedAverageDwellTime = this.props.latestAnalyticsForCampaign.weightedAverageDwellTimeForCampaign;

    return (
      <div className="campaign__row">
        <div className="campaign-box__header">Campaign performance Overview</div>
        <div className="campaign-box__body">
          <div id="metrics">
              <BigCardMetric metricLabel="Uniques"
                             metricTargetMessage={this.renderPercentageOfTarget(totalUniques, uniquesTarget)}
                             metricValue={totalUniques}
                             metricByDevice={uniquesPerDevice}/>

              <BigCardMetric metricLabel="Pageviews"
                             metricUnit=""
                             metricValue={totalPageviews}
                             metricByDevice={pageviewsPerDevice}/>

              <BigCardMetric metricLabel="Attention Time"
                             metricUnit="seconds"
                             metricValue={medianAttentionTime}
                             metricByDevice={medianPerDevice}/>

              <BigCardMetric metricLabel="Time on Page"
                             metricUnit="seconds"
                             metricValue={weightedAverageDwellTime}
                             metricByPath={averageDwellTimePerPathSeconds}/>
          </div>
        </div>
      </div>
    );
  }
}

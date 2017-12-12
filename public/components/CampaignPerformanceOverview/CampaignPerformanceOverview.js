import React, { PropTypes } from 'react';
import BigCardMetric from './BigCardMetric';
import {formatToMinutes} from '../../util/minutesFormatter';

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
    const totalUniques = this.props.latestAnalyticsForCampaign.uniques;
    const uniquesTarget = this.props.latestAnalyticsForCampaign && this.props.latestAnalyticsForCampaign.uniquesTarget;
    const medianAttentionTime = this.props.latestAnalyticsForCampaign.medianAttentionTimeSeconds;
    const weightedAverageDwellTimeInSeconds = this.props.latestAnalyticsForCampaign.weightedAverageDwellTimeForCampaign;

    return (
      <div className="campaign__row">
        <div className="campaign-box__header">Campaign performance Overview</div>
        <div className="campaign-box__body">
          <div id="metrics">
              <BigCardMetric metricLabel="Uniques"
                             metricTargetMessage={this.renderPercentageOfTarget(totalUniques, uniquesTarget)}
                             metricValue={totalUniques}/>

              <BigCardMetric metricLabel="Pageviews"
                             metricUnit=""
                             metricValue={totalPageviews}/>

              <BigCardMetric metricLabel="Attention Time"
                             metricUnit="seconds"
                             metricValue={medianAttentionTime}/>

              <BigCardMetric metricLabel="Time on Page"
                             metricValue={formatToMinutes(weightedAverageDwellTimeInSeconds)}/>
          </div>
        </div>
      </div>
    );
  }
}

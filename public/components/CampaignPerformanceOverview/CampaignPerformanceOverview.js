import React, { PropTypes } from 'react';
import R from 'ramda';

class AttentionTimePerPlatform extends React.Component {
  render() {
    const items = Object.keys(this.props.medianAttentionTimeSeconds).sort().map((deviceType) => {
      return (
        <div key={deviceType}>
          <label className="popover-key">{deviceType} :</label>
          <span className="popover-value">{this.props.medianAttentionTimeSeconds[deviceType]} seconds</span>
        </div>
      );
    });

    return (
      <div className="hover-popover">
        {items}
      </div>
    );
  }
}

class AverageDwellTimePerPath extends React.Component {
  render() {
    const items = Object.keys(this.props.averageDwellTimePerPathSeconds).sort().map((path) => {
      return (
        <div key={path}>
          <label className="popover-key">{path} :</label>
          <span className="popover-value">{this.props.averageDwellTimePerPathSeconds[path]} seconds</span>
        </div>
      );
    });

    return (
      <div className="hover-popover">
        {items}
      </div>
    );
  }
}


export default class CampaignPerformanceOverview extends React.Component {

  renderPercentageOfTarget(actual, target) {
    if (actual && target && target != 0) {
      return (
        "(" + Math.round(100*actual/target) +"% of target)"
      );
    } else { return (null); }
  }

  render () {

    const uniquesSoFar = this.props.latestAnalyticsForCampaign.uniques;
    const uniquesTarget = this.props.campaign.targets && this.props.campaign.targets.uniques;
    const pageviewsSoFar = this.props.latestAnalyticsForCampaign.pageviews;
    const pageviewsTarget = this.props.campaign.targets && this.props.campaign.targets.pageviews;

    const uniquesFromMobile = this.props.latestAnalyticsForCampaign.uniquesFromMobile;
    const uniquesFromDesktop = this.props.latestAnalyticsForCampaign.uniquesFromDesktop;

    const uniquesFromMobilePercentage = Math.round(100 * uniquesFromMobile/uniquesSoFar);
    const uniquesFromDesktopPercentage = Math.round(100 * uniquesFromDesktop/uniquesSoFar);

    const medianAttentionTime = this.props.latestAnalyticsForCampaign.medianAttentionTimeSeconds;
    const medianPerDevice = this.props.latestAnalyticsForCampaign.medianAttentionTimeByDevice || {};
    
    const weightedAverageDwellTime = this.props.latestAnalyticsForCampaign.weightedAverageDwellTimeForCampaign;
    const averageDwellTimePerPathSeconds = this.props.latestAnalyticsForCampaign.averageDwellTimePerPathSeconds || {};

    return (
      <div className="campaign-info campaign-box-section">
        <div className="campaign-box-section__header">
          Campaign Performance Overview
        </div>
        <div className="campaign-box-section__body">
          <div className="campaign-info__field">
            <label>Pageviews so far</label>
            <span className="campaign-info__field__value">{pageviewsSoFar ? pageviewsSoFar : "none available"} {this.renderPercentageOfTarget(pageviewsSoFar, pageviewsTarget)}</span>
          </div>
          <div className="campaign-info__field">
            <label>Uniques so far</label>
            <span className="campaign-info__field__value">{uniquesSoFar ? uniquesSoFar : "none available"} {this.renderPercentageOfTarget(uniquesSoFar, uniquesTarget)} {uniquesFromMobilePercentage && uniquesFromDesktopPercentage ? '(' + uniquesFromMobilePercentage + '% on mobile,' + uniquesFromDesktopPercentage + '% on desktop)' : ''}</span>
          </div>
          <div className="campaign-info__field">
            <label className={ R.isEmpty(medianPerDevice) ? "" : "hover" }>
              Median attention time (All platforms)
              <div className="hover-content">
                <AttentionTimePerPlatform medianAttentionTimeSeconds={ medianPerDevice } />
              </div>
            </label>
            <span className="campaign-info__field__value">{ medianAttentionTime ? `${medianAttentionTime} seconds` : "not available" }</span>
          </div>
          <div className="campaign-info__field">
            <label className={ R.isEmpty(averageDwellTimePerPathSeconds) ? "" : "hover" }>
              Weighted average dwell time
              <div className="hover-content">
                <AverageDwellTimePerPath averageDwellTimePerPathSeconds={ averageDwellTimePerPathSeconds } />
              </div>
            </label>
            <span className="campaign-info__field__value">{weightedAverageDwellTime ? `${weightedAverageDwellTime} seconds` : "none available"} </span>
          </div>
        </div>
      </div>
    );
  }


}

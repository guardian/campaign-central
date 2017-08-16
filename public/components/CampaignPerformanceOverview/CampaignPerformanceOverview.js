import React, { PropTypes } from 'react';

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
            <span className="campaign-info__field__value">{uniquesSoFar ? uniquesSoFar : "none available"} {this.renderPercentageOfTarget(uniquesSoFar, uniquesTarget)} ({uniquesFromMobilePercentage}% on mobile, {uniquesFromDesktopPercentage}% on desktop)</span>
          </div>
        </div>
      </div>
    );
  }


}

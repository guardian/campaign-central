import React, { PropTypes } from 'react';
import {ctaEditUrl} from '../../util/urlBuilder'

class CampaignCtas extends React.Component {



  getClickCount = (ctaId) => {
    if(this.props.campaignCtaStats) {
      var count = this.props.campaignCtaStats[ctaId];
      return count ? count : 0;
    }

    return '-';
  };

  getTotalClickCount = () => {
    if(this.props.campaignCtaStats) {
      var keys = Object.keys(this.props.campaignCtaStats);
      var total = 0;

      keys.forEach( (k) => total = total + this.props.campaignCtaStats[k]);
      return total;
    }

    return '-';
  };

  getCtr = (ctaId) => {
    if(this.props.campaignCtaStats && this.props.campaignAnalytics) {
      var count = this.props.campaignCtaStats[ctaId];
      if(!count) {count = 0}

      var latestStats = this.props.campaignAnalytics.pageCountStats[this.props.campaignAnalytics.pageCountStats.length - 1];
      var uniqueCount = latestStats["cumulative-unique-total"];

      if (uniqueCount && uniqueCount !== 0) {
        return ((count / uniqueCount) * 100 ).toFixed(2) + '%'
      }
    }

    return '-';
  };

  getTotalCtr = () => {
    if(this.props.campaignCtaStats && this.props.campaignAnalytics) {
      var keys = Object.keys(this.props.campaignCtaStats);
      var total = 0;

      keys.forEach( (k) => total = total + this.props.campaignCtaStats[k]);

      var latestStats = this.props.campaignAnalytics.pageCountStats[this.props.campaignAnalytics.pageCountStats.length - 1];
      var uniqueCount = latestStats["cumulative-unique-total"];

      if (uniqueCount && uniqueCount !== 0) {
        return ((total / uniqueCount) * 100 ).toFixed(2) + '%'
      }
    }

    return '-';
  };

  renderCtaItem = (cta) => {

    var ctaName = cta.trackingCode ? cta.trackingCode : cta.builderId;

    return (
      <div key={cta.builderId} className="campaign-cta-list__item">
        <div className="campaign-cta-list__row">
          <div className="campaign-cta-list__cta-tracking"><a href={ctaEditUrl(cta.builderId)} target="_blank">{ctaName}</a></div>
          <div className="campaign-cta-list__cta-clicks">{this.getClickCount(cta.builderId)}</div>
          <div className="campaign-cta-list__cta-ctr">{this.getCtr(cta.builderId)}</div>
        </div>
      </div>
    );
  }

  renderOverallStats = () => {
    if(this.props.campaign.callToActions.length > 1) {
      return (
        <div className="campaign-cta-list__item">
          <div className="campaign-cta-list__row">
            <div className="campaign-cta-list__cta-tracking">total</div>
            <div className="campaign-cta-list__cta-clicks">{this.getTotalClickCount()}</div>
            <div className="campaign-cta-list__cta-ctr">{this.getTotalCtr()}</div>
          </div>
        </div>
      );
    }

    return;
  }

  renderCtaInformation = () => {

    if(this.props.campaign.callToActions && this.props.campaign.callToActions.length > 0) {
      return (
        <div className="campaign-cta-list campaign-assets__field__value">
          <div className="campaign-cta-list__row">
            <div className="campaign-cta-list__cta-tracking--header">Tracking code</div>
            <div className="campaign-cta-list__cta-clicks--header">Clicks</div>
            <div className="campaign-cta-list__cta-ctr--header">CTR</div>
          </div>
          {this.props.campaign.callToActions.map( this.renderCtaItem ) }
          {this.renderOverallStats()}
        </div>
      )
    }

    return (
      <span className="campaign-assets__field__value">
        No call to actions configured yet
      </span>
    )
  }

  render () {
    return (
      <div className="campaign-assets__field">
        <label>Call to actions</label>
        {this.renderCtaInformation()}
      </div>
    );
  }
}

export default CampaignCtas

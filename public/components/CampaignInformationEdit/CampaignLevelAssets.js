import React, { PropTypes } from 'react';
import CampaignCtas from "./CampaignCtas";
import {tagEditUrl} from '../../util/urlBuilder';

class CampaignLevelAssets extends React.Component {

  componentWillMount() {
    this.props.campaignCtaStatsActions.getCampaignCtaStats(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign.id !== this.props.campaign.id) {
      this.props.campaignCtaStatsActions.getCampaignCtaStats(nextProps.campaign.id);
    }
  }

  getCtr = () => {
    if(this.props.campaignCtaStats && this.props.campaignPageViews) {
      var count = this.props.campaignCtaStats['logo'];
      if(!count) {count = 0}

      var latestStats = this.props.campaignPageViews.pageCountStats[this.props.campaignPageViews.pageCountStats.length - 1];
      var uniqueCount = latestStats["cumulative-unique-total"];

      var ctr = '';
      if (uniqueCount && uniqueCount !== 0) {
        ctr =  '(ctr: ' + ((count / uniqueCount) * 100 ).toFixed(2) + '%)';
      }

      return (<p>Logo clicks: {this.props.campaignCtaStats['logo']} {ctr}</p>);
    }

    return undefined;
  };


  renderTagInformation = () => {

    if(this.props.campaign.tagId) {
      return (
        <div>
        <span className="campaign-assets__field__value">
          <a href={tagEditUrl(this.props.campaign.tagId)} target="_blank">
            <img src={this.props.campaign.campaignLogo} className="campaign-assets__field__logo"/>
            {this.props.campaign.pathPrefix}
          </a>

        </span>
        <span className="campaign-assets__field__value">
          {this.getCtr()}
        </span>
        </div>
      )
    }

    return (
      <span className="campaign-assets__field__value">
        No tag has been configured yet
      </span>
    )
  };

  render () {

    var ctaBlock;
    if(this.props.campaign.type === 'hosted') {
      ctaBlock = (
        <CampaignCtas campaign={this.props.campaign} campaignCtaStats={this.props.campaignCtaStats} campaignAnalytics={this.props.campaignPageViews} />
      );
    }

    return (
      <div className="campaign-assets">
        <div className="campaign-assets__field">
          <label>Tag</label>
          {this.renderTagInformation()}
        </div>

        {ctaBlock}
      </div>
    );
  }
}


//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCampaignCtaStats from '../../actions/CampaignActions/getCampaignCtaStats';

function mapStateToProps(state) {
  return {
    campaignPageViews: state.campaignPageViews,
    campaignCtaStats: state.campaignCtaStats
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignCtaStatsActions: bindActionCreators(Object.assign({}, getCampaignCtaStats), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignLevelAssets);

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

  renderTagInformation = () => {

    if(this.props.campaign.tagId) {
      return (
        <span className="campaign-assets__field__value">
          <a href={tagEditUrl(this.props.campaign.tagId)} target="_blank">
            <img src={this.props.campaign.campaignLogo} className="campaign-assets__field__logo"/>
            {this.props.campaign.pathPrefix}
          </a>
        </span>
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
        <CampaignCtas campaign={this.props.campaign} campaignCtaStats={this.props.campaignCtaStats} campaignAnalytics={this.props.campaignAnalytics} />
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
    campaignAnalytics: state.campaignAnalytics,
    campaignCtaStats: state.campaignCtaStats
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignCtaStatsActions: bindActionCreators(Object.assign({}, getCampaignCtaStats), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignLevelAssets);

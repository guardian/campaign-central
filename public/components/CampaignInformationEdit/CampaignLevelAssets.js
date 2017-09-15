import React, { PropTypes } from 'react';
import {tagEditUrl} from '../../util/urlBuilder';

class CampaignLevelAssets extends React.Component {

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign.id !== this.props.campaign.id) {
    }
  }

  getCtr = () => {
    if(this.props.campaignCtaStats && this.props.campaignPageViews && this.props.campaignPageViews.pageCountStats) {
      let count = this.props.campaignCtaStats['logo'];
      if(!count) {count = 0}

      const latestStats = this.props.campaignPageViews.pageCountStats[this.props.campaignPageViews.pageCountStats.length - 1];
      const uniqueCount = latestStats["cumulative-unique-total"];

      let ctr = '';
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
    return (
      <div className="campaign-assets">
        <div className="campaign-assets__field">
          <label>Tag</label>
          {this.renderTagInformation()}
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
    campaignPageViews: state.campaignPageViews,
  };
}

function mapDispatchToProps(dispatch) {
  return {

  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignLevelAssets);

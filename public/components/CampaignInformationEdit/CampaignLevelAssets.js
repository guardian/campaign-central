import React, { PropTypes } from 'react';
import CampaignCtas from "./CampaignCtas";
import {tagEditUrl} from '../../util/urlBuilder';

class CampaignLevelAssets extends React.Component {

  componentWillMount() {

  }

  componentWillReceiveProps(nextProps) {

  }

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

  };
}

function mapDispatchToProps(dispatch) {
  return {

  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignLevelAssets);

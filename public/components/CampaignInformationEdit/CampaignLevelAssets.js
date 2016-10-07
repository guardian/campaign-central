import React, { PropTypes } from 'react';
import {getStore} from '../../util/storeAccessor'

class CampaignLevelAssets extends React.Component {



  renderTagInformation = () => {
    const store = getStore();
    const tagManagerUrl = store.getState().config.tagManagerUrl;

    if(this.props.campaign.tagId) {
      return (
        <span className="campaign-assets__field__value">
          <a href={tagManagerUrl + '/tag/' + this.props.campaign.tagId} target="_blank">
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
  }

  renderCtaInformation = () => {
    const store = getStore();
    const ctaAtomMakerUrl = store.getState().config.ctaAtomMakerUrl;

    if(this.props.campaign.callToActions && this.props.campaign.callToActions.length > 0) {
      return (
        <span className="campaign-assets__field__value">
          <ul>
            {this.props.campaign.callToActions.map( cta => <li key={cta.builderId}><a href={ctaAtomMakerUrl + '/#/atom/' + cta.builderId} target="_blank">{cta.builderId}</a></li> )}
          </ul>
        </span>
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
      <div className="campaign-assets campaign-box-section">
        <div className="campaign-box-section__header">
          Campaign Level Assets
        </div>
        <div className="campaign-box-section__body">
          <div className="campaign-assets__field">
            <label>Tag</label>
            {this.renderTagInformation()}
          </div>
          <div className="campaign-assets__field">
            <label>Call to actions</label>
            {this.renderCtaInformation()}
          </div>
        </div>
      </div>
    );
  }
}

export default CampaignLevelAssets;
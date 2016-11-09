import React, { PropTypes } from 'react';
import {tagEditUrl, ctaEditUrl} from '../../util/urlBuilder'

class CampaignLevelAssets extends React.Component {

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
  }

  renderCtaInformation = () => {

    if(this.props.campaign.callToActions && this.props.campaign.callToActions.length > 0) {
      return (
        <span className="campaign-assets__field__value">
          <ul>
            {this.props.campaign.callToActions.map( cta => {
              var ctaName = cta.trackingCode ? cta.trackingCode : cta.builderId;
              return (<li key={cta.builderId}><a href={ctaEditUrl(cta.builderId)} target="_blank">{ctaName}</a></li>)
            })}
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

    var ctaBlock;
    if(this.props.campaign.type === 'hosted') {
      ctaBlock = (
        <div className="campaign-assets__field">
          <label>Call to actions</label>
          {this.renderCtaInformation()}
        </div>
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

export default CampaignLevelAssets;

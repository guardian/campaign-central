import React from "react";
import ProgressSpinner from "../../utils/ProgressSpinner";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as getCampaignReferrals from "../../../actions/CampaignActions/getCampaignReferrals";

class CampaignReferrals extends React.Component {

  componentWillMount() {
    this.props.campaignReferralActions.getCampaignReferrals(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    const campaignChanged = nextProps.campaign.id !== this.props.campaign.id;
    if (campaignChanged) {
      this.props.campaignReferralActions.getCampaignReferrals(nextProps.campaign.id);
    }
  }

  renderReferral = (referral, index) => {

    const dateFormat = (date) => {
      return new Date(date).toLocaleDateString('en-GB', {day: '2-digit', month: 'short', year: 'numeric'})
    };

    return (
      <div key={index} className="campaign-referral-list__item">
        <div className="campaign-referral-list__row">
          <div className="campaign-referral-list__platform">{referral.component.platform}</div>
          <div className="campaign-referral-list__edition">{referral.component.edition}</div>
          <div className="campaign-referral-list__path">{referral.component.path}</div>
          <div className="campaign-referral-list__container">#{referral.component.containerIndex}: {referral.component.containerName}</div>
          <div className="campaign-referral-list__card">#{referral.component.cardIndex}: {referral.component.cardName}</div>
          <div className="campaign-referral-list__clicks">{referral.numClicks}</div>
          <div className="campaign-referral-list__date">{dateFormat(referral.firstReferral)}</div>
          <div className="campaign-referral-list__date">{dateFormat(referral.lastReferral)}</div>
        </div>
      </div>
    );
  };

  render() {

    if(!this.props.campaignReferrals) {
      return (
        <div className="campaign-info campaign-box">
          <div className="campaign-box__header">Referrals from on-platform</div>
          <div className="campaign-box__body">
            <ProgressSpinner/>
          </div>
        </div>
      );
    }

    if(this.props.campaignReferrals.length > 0) {
      return (
        <div className="campaign-info campaign-box">
          <div className="campaign-box__header">Referrals from on-platform</div>
          <div className="campaign-box__body">
            <div className="campaign-referral-list campaign-assets__field__value">
              <div className="campaign-referral-list__row">
                <div className="campaign-referral-list__platform--header">Platform</div>
                <div className="campaign-referral-list__edition--header">Edition</div>
                <div className="campaign-referral-list__path--header">Path</div>
                <div className="campaign-referral-list__container--header">Container</div>
                <div className="campaign-referral-list__card--header">Card</div>
                <div className="campaign-referral-list__clicks--header">Clicks</div>
                <div className="campaign-referral-list__date--header">First referral</div>
                <div className="campaign-referral-list__date--header">Last referral</div>
              </div>
              {this.props.campaignReferrals.map(this.renderReferral)}
            </div>
          </div>
        </div>
      );
    }

    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header">Referrals from on-platform</div>
        <div className="campaign-box__body">
          <span className="campaign-assets__field__value">No traffic has been referred from on-platform to this campaign yet.</span>
        </div>
      </div>
    )
  };
}

function mapStateToProps(state) {
  return {
    campaignReferrals: state.campaignReferrals
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignReferralActions: bindActionCreators(Object.assign({}, getCampaignReferrals), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignReferrals);

import React from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {setToggleOrder} from "../../../actions/CampaignActions/getOnPlatformReferrals";

class OnPlatformReferralHeader extends React.Component {

    onHeaderClick(fieldName){
        this.props.campaignToggleOrderAction.setToggleOrder(fieldName);
    }

    render() {
        let orderingArrow = {
            impressions: '',
            clicks: '',
            ctr: ''
        };
      orderingArrow[this.props.onPlatformReferrals.ordering.field] = `campaign-referral-list__header--${this.props.onPlatformReferrals.ordering.order}`;
        return (
            <div className="pure-g campaign-referral-list__row">
              <div className="pure-u-15-24 campaign-referral-list__header">From</div>
              <div className={`pure-u-3-24 campaign-referral-list__header campaign-referral-list__header--toggle ${orderingArrow['impressions']}`} onClick={this.onHeaderClick.bind(this, 'impressions')}>Impressions</div>
              <div className={`pure-u-3-24 campaign-referral-list__header campaign-referral-list__header--toggle ${orderingArrow['clicks']}`} onClick={this.onHeaderClick.bind(this, 'clicks')}>Clicks</div>
              <div className={`pure-u-3-24 campaign-referral-list__header campaign-referral-list__header--toggle ${orderingArrow['ctr']}`} onClick={this.onHeaderClick.bind(this, 'ctr')}>CTR (%)</div>
            </div>
        );
    }
}

function mapStateToProps(state) {
  return {
    onPlatformReferrals: state.onPlatformReferrals
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignToggleOrderAction: bindActionCreators(Object.assign({}, {
      setToggleOrder: setToggleOrder
    }), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(OnPlatformReferralHeader)

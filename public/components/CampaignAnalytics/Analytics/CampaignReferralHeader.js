import React from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {setToggleOrder} from "../../../actions/CampaignActions/getCampaignReferrals";

class CampaignReferralHeader extends React.Component {

    componentWillMount() {
        this.props.campaignToggleOrderAction.setToggleOrder();
    }

    onHeaderClick(fieldName){
        this.props.campaignToggleOrderAction.setToggleOrder(fieldName);
    }

    render() {
        let orderingArrow = {
            impressions: '',
            clicks: '',
            ctr: ''
        };
        orderingArrow[this.props.campaignReferralOrder.field] = `campaign-referral-list__header--${this.props.campaignReferralOrder.order}`;
        return (
            <div className="pure-g campaign-referral-list__row">
              <div className="pure-u-17-24 campaign-referral-list__header">From</div>
              <div className={`pure-u-3-24 campaign-referral-list__header campaign-referral-list__header--toggle ${orderingArrow['impressions']}`} onClick={this.onHeaderClick.bind(this, 'impressions')}>Impressions</div>
              <div className={`pure-u-2-24 campaign-referral-list__header campaign-referral-list__header--toggle ${orderingArrow['clicks']}`} onClick={this.onHeaderClick.bind(this, 'clicks')}>Clicks</div>
              <div className={`pure-u-2-24 campaign-referral-list__header campaign-referral-list__header--toggle ${orderingArrow['ctr']}`} onClick={this.onHeaderClick.bind(this, 'ctr')}>CTR (%)</div>
            </div>
        );
    }
}

function mapStateToProps(state) {
  return {
    campaignReferralOrder: state.campaignReferralOrder
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignToggleOrderAction: bindActionCreators(Object.assign({}, {
      setToggleOrder: setToggleOrder
    }), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignReferralHeader)
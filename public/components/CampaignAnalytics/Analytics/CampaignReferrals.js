import React from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {getCampaignReferrals} from "../../../actions/CampaignActions/getCampaignReferrals";
import CampaignReferralHeader from "./CampaignReferralHeader";
import InfinityMenu from "react-infinity-menu";
import ProgressSpinner from "../../utils/ProgressSpinner";

class CampaignReferrals extends React.Component {

  componentWillMount() {
    this.props.campaignReferralActions.getCampaignReferrals(this.props.campaign.id);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.campaign.id !== this.props.campaign.id) {
      this.props.campaignReferralActions.getCampaignReferrals(nextProps.campaign.id);
    }
  }

  onNodeMouseClick(event, tree, node, level, keyPath) {
    console.log('on node mouse click ', event, tree, node, level, keyPath);
  }

  render() {

    if (this.props.campaignReferrals &&
        this.props.campaignReferrals.tree &&
        this.props.campaignReferrals.tree.length === 0) {
      return (
        <div className="campaign-info campaign-box">
          <div className="campaign-box__header">Referrals from on-platform</div>
          <div className="campaign-box__body">There are currently no on-platform referrals recorded for this campaign.
          </div>
        </div>
      )
    }

    if (this.props.campaignReferrals && this.props.campaignReferrals.tree) {
      return (
        <div className="campaign-info campaign-box">
          <div className="campaign-box__header">Referrals from on-platform</div>
          <div className="campaign-box__body">
            <div className="campaign-referral-list campaign-assets__field__value">
              <InfinityMenu
                tree={this.props.campaignReferrals.tree}
                disableDefaultHeaderContent={true}
                headerContent={CampaignReferralHeader}
                onNodeMouseClick={this.onNodeMouseClick.bind(this)}
              />
            </div>
          </div>
        </div>
      );
    }

    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header">Referrals from on-platform</div>
        <div className="campaign-box__body">
          <ProgressSpinner/>
        </div>
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    campaignReferrals: state.campaignReferrals
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignReferralActions: bindActionCreators(Object.assign({}, {
      getCampaignReferrals: getCampaignReferrals
    }), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignReferrals);

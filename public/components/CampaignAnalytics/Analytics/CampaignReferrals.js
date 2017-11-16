import React from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {getCampaignReferrals} from "../../../actions/CampaignActions/getCampaignReferrals";
import CampaignReferral from "./CampaignReferral";
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

  componentDidUpdate(prevProps) {
    if (this.props.campaignReferrals && prevProps.campaignReferrals !== this.props.campaignReferrals) {
      this.addTreeToState(this.props.campaignReferrals)
    }
  }

  buildTree = (referrals) => {
    let len = referrals.length,
      tree = [],
      i;

    for (i = 0; i < len; i += 1) {

      const children =
        (referrals[i] && referrals[i].children && referrals[i].children.length > 0) ?
          this.buildTree(referrals[i].children) :
          [];

      tree.push({
        "name": referrals[i].sourceDescription,
        "id": i,
        "impressionCount": referrals[i].stats.impressionCount,
        "clickCount": referrals[i].stats.clickCount,
        "ctr": referrals[i].stats.ctr,
        "customComponent": CampaignReferral,
        "isOpen": false,
        "children": children
      });
    }

    return tree;
  };

  addTreeToState = (referrals) => {
    this.setState({
      tree: this.buildTree(referrals),
      level: 0
    });
  };

  onNodeMouseClick(event, tree) {
    this.setState({
      tree: tree
    });
  }

  render() {

    if (this.state && this.state.tree && this.state.tree.length === 0) {
      return (
        <div className="campaign-info campaign-box">
          <div className="campaign-box__header">Referrals from on-platform</div>
          <div className="campaign-box__body">There are currently no on-platform referrals recorded for this campaign.
          </div>
        </div>
      )
    }

    if (this.state && this.state.tree) {
      return (
        <div className="campaign-info campaign-box">
          <div className="campaign-box__header">Referrals from on-platform</div>
          <div className="campaign-box__body">
            <div className="campaign-referral-list campaign-assets__field__value">
              <InfinityMenu
                tree={this.state.tree}
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
    campaignReferralActions: bindActionCreators(Object.assign({}, getCampaignReferrals), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignReferrals);

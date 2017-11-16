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

    this.addTreeToState(nextProps.campaignReferrals, nextProps.campaignReferralOrder);
  }

  buildTree = (referrals, ordering) => {

    let len = referrals.length,
      tree = [],
      i;

    for (i = 0; i < len; i += 1) {

      const children =
        (referrals[i] && referrals[i].children && referrals[i].children.length > 0) ?
          this.buildTree(referrals[i].children, ordering) :
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

    // Sort children
    if (ordering) {
      const {order, field} = ordering;

      tree.sort((a, b) => {

        const compare = (numA, numB) => {
          const invert = order === 'asc' ? 1 : -1;
          if (numA < numB) {
            return -1 * invert;
          } else if (numA > numB) {
            return invert;
          } else {
            return 0;
          }
        };

        switch (field) {
          case 'impressions':
            return compare(a.impressionCount, b.impressionCount);
          case 'clicks':
            return compare(a.clickCount, b.clickCount);
          case 'ctr':
            return compare(a.ctr, b.ctr);
        }
      });
    }

    return tree;
  };

  addTreeToState = (referrals, ordering) => {
    this.setState({
      tree: this.buildTree(referrals, ordering)
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
    campaignReferrals: state.campaignReferrals,
    campaignReferralOrder: state.campaignReferralOrder
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

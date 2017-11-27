import React from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {getCampaignReferrals, setToggleNode} from "../../../actions/CampaignActions/getCampaignReferrals";
import CampaignReferralHeader from "./CampaignReferralHeader";
import InfinityMenu from "react-infinity-menu";
import ProgressSpinner from "../../utils/ProgressSpinner";
import DateRangeEditor from "./DateRangeEditor";

class CampaignReferrals extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      dateRange: {
        startDate: DateRangeEditor.toDate(this.props.campaign.startDate),
        endDate: DateRangeEditor.toDate(this.props.campaign.endDate)
      }
    };
  }

  componentWillMount() {
    this.props.campaignReferralActions.getCampaignReferrals(this.props.campaign.id, this.state.dateRange);
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextProps.campaign.id !== this.props.campaign.id || nextState !== this.state) {
      this.props.campaignReferralActions.getCampaignReferrals(nextProps.campaign.id, nextState.dateRange);
    }
  }

  onNodeMouseClick() {
    this.props.campaignReferralActions.setToggleNode();
  }

  onDateRangeChange = (dateRange) => {
    this.setState({
      dateRange: dateRange
    });
  };

  renderBody() {

    if (this.props.campaignReferrals &&
      this.props.campaignReferrals.tree &&
      this.props.campaignReferrals.tree.length === 0) {
      return (
        <div>There are no on-platform referrals recorded for this campaign and date range.</div>
      )
    }

    if (this.props.campaignReferrals && this.props.campaignReferrals.tree) {
      return (
        <div className="campaign-referral-list campaign-assets__field__value">
          <InfinityMenu
            tree={this.props.campaignReferrals.tree}
            disableDefaultHeaderContent={true}
            headerContent={CampaignReferralHeader}
            onNodeMouseClick={this.onNodeMouseClick.bind(this)}
          />
        </div>
      );
    }

    return (
      <ProgressSpinner/>
    );
  }

  render() {
    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header campaign-referral-box-header">
          Referrals from on-platform
          <DateRangeEditor campaign={this.props.campaign} onChange={this.onDateRangeChange}/>
        </div>
        <div className="campaign-box__body">
          {this.renderBody()}
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
      getCampaignReferrals: getCampaignReferrals,
      setToggleNode: setToggleNode
    }), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignReferrals);

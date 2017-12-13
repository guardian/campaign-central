import React from "react";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {getOnPlatformReferrals, setToggleNode} from "../../../actions/CampaignActions/getOnPlatformReferrals";
import {getSocialReferrals} from "../../../actions/CampaignActions/getSocialReferrals";
import DateRangeEditor from "./DateRangeEditor";
import SocialReferrals from "./SocialReferrals";
import OnPlatformReferrals from "./OnPlatformReferrals";

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
    this.refresh(this.props, this.state);
  }

  componentWillUpdate(nextProps, nextState) {
    if (nextState.dateRange !== this.state.dateRange) {
      this.refresh(nextProps, nextState);
    }
  }

  refresh(props, state) {
    this.props.campaignReferralActions.getOnPlatformReferrals(props.campaign.id, state.dateRange);
    this.props.campaignReferralActions.getSocialReferrals(props.campaign.id, state.dateRange, props.territory);
  }

  onNodeMouseClick() {
    this.props.campaignReferralActions.setToggleNode();
  }

  onDateRangeChange = (dateRange) => {
    this.setState({
      dateRange: dateRange
    });
  };

  render() {
    return (
      <div className="campaign-info campaign-box">
        <div className="campaign-box__header campaign-referral-box-header">
          Referrals
          <DateRangeEditor campaign={this.props.campaign} onChange={this.onDateRangeChange}/>
        </div>
        <div id="campaign-referrals-body" className="campaign-box__body">
          <OnPlatformReferrals referrals={this.props.onPlatformReferrals}
                               onNodeMouseClick={this.onNodeMouseClick.bind(this)}/>
          <SocialReferrals referrals={this.props.socialReferrals}/>
        </div>
      </div>
    );
  }
}

function mapStateToProps(state) {
  return {
    onPlatformReferrals: state.onPlatformReferrals,
    socialReferrals: state.socialReferrals
  };
}

function mapDispatchToProps(dispatch) {
  return {
    campaignReferralActions: bindActionCreators(Object.assign({}, {
      getOnPlatformReferrals: getOnPlatformReferrals,
      setToggleNode: setToggleNode,
      getSocialReferrals: getSocialReferrals
    }), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(CampaignReferrals);

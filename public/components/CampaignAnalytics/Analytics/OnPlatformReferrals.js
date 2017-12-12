import React from "react";
import OnPlatformReferralHeader from "./OnPlatformReferralHeader";
import InfinityMenu from "react-infinity-menu";
import ProgressSpinner from "../../utils/ProgressSpinner";

class OnPlatformReferrals extends React.Component {

  renderBody() {

    if (this.props.referrals &&
      this.props.referrals.tree &&
      this.props.referrals.tree.length === 0) {
      return (
        <div>There are no on-platform referrals recorded for this campaign and date range.</div>
      )
    }

    if (this.props.referrals && this.props.referrals.tree) {
      return (
        <div className="campaign-referral-list">
          <InfinityMenu
            tree={this.props.referrals.tree}
            disableDefaultHeaderContent={true}
            headerContent={OnPlatformReferralHeader}
            onNodeMouseClick={this.props.onNodeMouseClick}
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
      <section className="campaign-assets__field__value">
        <h3>On Platform</h3>
        {this.renderBody()}
      </section>
    );
  }
}

export default OnPlatformReferrals;

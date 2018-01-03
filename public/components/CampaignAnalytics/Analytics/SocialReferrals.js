import React from "react";
import ProgressSpinner from "../../utils/ProgressSpinner";

class SocialReferrals extends React.Component {

  static renderRow(referral, index) {
    return (
      <tr key={index} className="campaign-referral-list__item">
        <td>{referral.referringPlatform}</td>
        <td className="numeric-value">{referral.organicClickCount.toLocaleString()}</td>
        <td className="numeric-value">{referral.paidClickCount.toLocaleString()}</td>
      </tr>
    )
  }

  renderBody() {

    if (this.props.referrals && this.props.referrals.length === 0) {
      return (
        <div>There are no social referrals recorded for this campaign and date range.</div>
      )
    }

    if (this.props.referrals) {
      return (
        <table className="pure-table">
          <thead>
          <tr>
            <th>Platform</th>
            <th>Organic clicks</th>
            <th>Paid clicks</th>
          </tr>
          </thead>
          <tbody>
          {this.props.referrals.map((referral, index) => SocialReferrals.renderRow(referral, index))}
          </tbody>
        </table>
      )
    }

    return (
      <ProgressSpinner/>
    );
  }

  render() {
    return (
      <section className="campaign-assets__field__value">
        <h3>Social</h3>
        {this.renderBody()}
      </section>
    )
  }
}

export default SocialReferrals;

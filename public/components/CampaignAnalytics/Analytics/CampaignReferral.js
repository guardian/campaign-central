import React from "react";
import NumberFormat from 'react-number-format';
import "react-infinity-menu/src/infinity-menu.css";

export default class CampaignReferral extends React.Component {

  static explainImpossibleCTR(ctr) {
    if (ctr > 1) {
      const explanation = "The behaviour of the back button in the Safari browser is anomalous;"+
        " following a link from a front, then using the back button" +
        " to go back to the front, then following the same link again" +
        " is being counted as a single impression and two clicks rather than two impressions and two clicks." +
        " This should have a negligible effect except where the number of impressions is very low," +
        " as in this case.";
      return <span title={explanation}><i className="i-warning"/></span>
    }
    return "";
  };

  render() {
    // The keyPath needs to be halved because it is typically something like:
    // '0.children.0.children.0.children.0.children.0'
    // should give a depth of 4.
    const depth = Math.floor(this.props.data.keyPath.split(".").length / 2);
    return (
      <li key={this.props.data.keyPath} className="pure-g campaign-referral-list" onClick={this.props.onClick}>
        <div className={'pure-u-'+depth+'-24'} />
        <div className={'pure-u-' + (15 - depth) + '-24 campaign-referral-list__source'}>{this.props.data.name}</div>
        <div className="pure-u-3-24 campaign-referral-list__impressions">
          <NumberFormat value={this.props.data.impressionCount} displayType={'text'} thousandSeparator={true}/>
        </div>
        <div className="pure-u-3-24 campaign-referral-list__clicks">
          <NumberFormat value={this.props.data.clickCount} displayType={'text'} thousandSeparator={true}/>
        </div>
        <div className="pure-u-3-24 campaign-referral-list__ctr">
          <NumberFormat value={this.props.data.ctr * 100} displayType={'text'} decimalPrecision={2}/>
          {CampaignReferral.explainImpossibleCTR(this.props.data.ctr)}
        </div>
      </li>
    );
  }
}

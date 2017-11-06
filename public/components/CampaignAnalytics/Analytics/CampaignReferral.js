import React from "react";
import NumberFormat from 'react-number-format';
import "react-infinity-menu/src/infinity-menu.css";

export default class CampaignReferral extends React.Component {

  render() {
    const depth = Math.floor(this.props.data.keyPath.split(".").length / 2);
    return (
      <li key={this.props.data.keyPath} className="pure-g campaign-referral-list" onClick={this.props.onClick}>
        <div className={'pure-u-'+depth+'-24'} />
        <div className={'pure-u-'+(17-depth)+'-24 campaign-referral-list__source'}>{this.props.data.name}</div>
        <div className="pure-u-3-24 campaign-referral-list__impressions">
          <NumberFormat value={this.props.data.impressionCount} displayType={'text'} thousandSeparator={true}/>
        </div>
        <div className="pure-u-2-24 campaign-referral-list__clicks">
          <NumberFormat value={this.props.data.clickCount} displayType={'text'} thousandSeparator={true}/>
        </div>
        <div className="pure-u-2-24 campaign-referral-list__ctr">
          <NumberFormat value={this.props.data.ctr * 100} displayType={'text'} decimalPrecision={2}/>
        </div>
      </li>
    );
  }
}

import React, { PropTypes } from 'react';

export default class BigCardMetric extends React.Component {

  render() {
    if (!this.props.metricValue) return null;
    return (
      <div className="box">
          <div className="head">{this.props.metricLabel}</div>
          <div className="count ">{this.props.metricValue.toLocaleString()}</div>
          <div className="unit">{this.props.metricUnit ? this.props.metricUnit : this.props.metricTargetMessage}</div>
      </div>
    );
  }
}

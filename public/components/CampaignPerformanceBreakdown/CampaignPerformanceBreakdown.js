import React, { PropTypes } from 'react';
import {formatToMinutes} from '../../util/minutesFormatter';

class CampaignPerformanceBreakdownTable extends React.Component {

  render() {

    const analytics = Object.entries(this.props.analyticsBreakdown || {});
    const dataUnavailable = 'Unavailable';

    return (
      <table className="pure-table">
        <thead>
        <tr>
          <th>{this.props.breakdownLabel}</th>
          <th>Unique Users</th>
          <th>Total Page Views</th>
          <th>Average Time on Page(minutes)</th>
        </tr>
        </thead>

        <tbody>
        {analytics.map(([breakdownKey, values]) => {
          return(
            <tr key={breakdownKey}>
              <td>{breakdownKey}</td>
              <td>{values.uniques ? values.uniques : dataUnavailable}</td>
              <td>{values.pageviews ? values.pageviews : dataUnavailable}</td>
              <td>{values.timeSpentOnPage ? formatToMinutes(values.timeSpentOnPage) : dataUnavailable}</td>
            </tr>
          );

        })}
        </tbody>
      </table>
    );
  }
}

export default class CampaignPerformanceBreakdown extends React.Component {

  view = {
    'LOCATION': 'LOCATION',
    'DEVICE': 'DEVICE',
    'PATH': 'PATH'
  };

  constructor(props) {
    super(props);

    this.state = {
      currentView: this.view.LOCATION
    }
  }

  onViewChange = (e, selectedView) => {
    this.setState({
      currentView: selectedView
    });
  }

  renderBreakdownTable() {
    const analyticsByCountryCode = this.props.latestAnalyticsForCampaign.analyticsByCountryCode || {};
    const analyticsByDevice = this.props.latestAnalyticsForCampaign.analyticsByDevice || {};
    const analyticsByPath = this.props.latestAnalyticsForCampaign.analyticsByPath || {};

    switch(this.state.currentView) {
      case this.view.LOCATION:
        return(<CampaignPerformanceBreakdownTable breakdownLabel="Country" analyticsBreakdown={analyticsByCountryCode}/>);
      case this.view.DEVICE:
        return(<CampaignPerformanceBreakdownTable breakdownLabel="Device" analyticsBreakdown={analyticsByDevice}/>);
      case this.view.PATH:
        return(<CampaignPerformanceBreakdownTable breakdownLabel="Path" analyticsBreakdown={analyticsByPath}/>);
      default:
        return(<CampaignPerformanceBreakdownTable breakdownLabel="Country" analyticsBreakdown={analyticsByCountryCode}/>);
    }
  }

  render () {

    return(
      <div className="campaign__row">
        <div className="campaign-box__header">Campaign performance Breakdown</div>
        <div className="campaign-box__body">

        <div id ="performance-breakdown-nav" className="pure-button-group" role="group" aria-label="...">
          <button className={this.state.currentView === this.view.LOCATION ? 'pure-button pure-button-active' : 'pure-button'} onClick={(e) => this.onViewChange(e, this.view.LOCATION)}>Location</button>
          <button className={this.state.currentView === this.view.DEVICE ? 'pure-button pure-button-active' : 'pure-button'} onClick={(e) => this.onViewChange(e, this.view.DEVICE)}>Device</button>
          <button className={this.state.currentView === this.view.PATH ? 'pure-button pure-button-active' : 'pure-button'} onClick={(e) => this.onViewChange(e, this.view.PATH)}>Path</button>
        </div>

        {this.renderBreakdownTable()}

      </div>
      </div>
    );

  }
}

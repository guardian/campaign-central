import React, {Component, PropTypes} from 'react';
import BigCardMetric from '../CampaignPerformanceOverview/BigCardMetric';
import {formatToMinutes} from '../../util/minutesFormatter';


class BenchmarkSet extends Component {

  render() {
    const totals = this.props.totals;
    const averages = this.props.averages;
    const medians = this.props.medians;

    return(
      <div className="campaign__row">
        <div className="campaign-box__header">{this.props.title}</div>
        <div className="campaign-box__body">
          <div id="metrics">
            <BigCardMetric metricLabel="Total Uniques"
                           metricValue={totals.uniques}/>

            <BigCardMetric metricLabel="Total Pageviews"
                           metricValue={totals.pageviews}/>

            <BigCardMetric metricLabel="Total Time on Page"
                           metricValue={formatToMinutes(totals.timeSpentOnPage)}/>

            <BigCardMetric metricLabel="Average Uniques"
                           metricValue={averages.uniques}/>

            <BigCardMetric metricLabel="Average Pageviews"
                           metricValue={averages.pageviews}/>

            <BigCardMetric metricLabel="Average Time on Page"
                           metricValue={formatToMinutes(averages.timeSpentOnPage)}/>

            <BigCardMetric metricLabel="Median Attention Time"
                           metricValue={formatToMinutes(medians.attentionTime)}/>
          </div>
        </div>
      </div>
    );

  }
}

class Benchmarks extends Component {

  componentDidMount() {
    this.props.benchmarkActions.getBenchmarks();
  }

  render() {
    const benchmarks = this.props.benchmarks;

    if (!benchmarks > 0) { return(null) }

    return (
      <div>
        <h1>Benchmarks</h1>

        <p style={{lineHeight: 1.5}}>
          Benchmarks provides an aggregate of the metrics we provide per campaign across all campaigns, including the subsets by campaign type.
          This data relies on the data per each individual campaign. Therefore if campaigns are missing, their campaign type is incorrect or the start and end date for the
          campaign are not set correctly in the Tag Manager then this will have an underlining effect on these numbers.
        </p>

        <BenchmarkSet totals={benchmarks.totals} averages={benchmarks.averages} medians={benchmarks.medians} title="Across All Campaigns" />
        <BenchmarkSet totals={benchmarks.paidFor.totals} averages={benchmarks.paidFor.averages} medians={benchmarks.paidFor.medians} title="Across Paid For Campaigns" />
        <BenchmarkSet totals={benchmarks.hosted.totals} averages={benchmarks.hosted.averages} medians={benchmarks.hosted.medians} title="Across Hosted Campaigns" />
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getAllCampaignBenchmarks from '../../actions/CampaignActions/getAllCampaignBenchmarks';

function mapStateToProps(state) {
  return {
    benchmarks: state.benchmarks
  };
}

function mapDispatchToProps(dispatch) {
  return {
    benchmarkActions: bindActionCreators(Object.assign({}, getAllCampaignBenchmarks), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Benchmarks);

import React, { PropTypes } from 'react'
import {RadialBarChart, RadialBar, Legend, ResponsiveContainer, Tooltip} from 'recharts'
import {formatPath, getFillColour} from '../../../util/analyticsHelper'
import {pageCountStatPropType} from '../../../propTypes/analytics'

class CampaignPerformanceSummary extends React.Component {

  static propTypes = {
    latestPageViews: pageCountStatPropType.isRequired,
    campaign: PropTypes.shape({
      targets: PropTypes.shape({
        uniques: PropTypes.number
      })
    }),
    paths: PropTypes.arrayOf(PropTypes.string).isRequired
  };

  getCampaignTargetUniques() {
    return this.props.campaign.targets ? this.props.campaign.targets.uniques : undefined
  }

  getUniquesTargetToDate() {
    if (this.props.targets && this.props.targets.targets.uniques) {
      const uniqueTargetData = this.props.targets.targets.uniques;
      const latestTarget = uniqueTargetData.runRate[uniqueTargetData.runRate.length -1];
      return latestTarget.expected;
    }
    return undefined;
  }

  getEstimatedUniques() {
    return this.props.latestDailyUniques ? this.props.latestDailyUniques.cumulativeUniqueUsers : undefined
  }

  getCampaignTargetPageViews() {
    return this.props.campaign.targets ? this.props.campaign.targets.pageviews : undefined
  }

  getPageViewsTargetToDate() {
    if (this.props.targets && this.props.targets.targets.pageviews) {
      const pageviewsTargetData = this.props.targets.targets.pageviews;
      const latestTarget = pageviewsTargetData.runRate[pageviewsTargetData.runRate.length -1];
      return latestTarget.expected;
    }
    return undefined;
  }

  getPageViews() {
    return this.props.latestPageViews ? this.props.latestPageViews["cumulative-count-total"] : undefined
  }

  buildContributionData() {
    var index = 0;
    var data = [];

    var targetUniques = this.getCampaignTargetUniques();
    if (targetUniques) {
      data.push( {name: "target uniques", count: targetUniques, fill: getFillColour(index++)} );
    }

    var targetUniquesToDate = this.getUniquesTargetToDate();
    if (targetUniquesToDate) {
      data.push( {name: "target uniques to date", count: targetUniquesToDate, fill: getFillColour(index++)} );
    }

    var estimatedUniques = this.getEstimatedUniques();
    if (estimatedUniques) {
      data.push( {name: "estimated Uniques", count: estimatedUniques, fill: getFillColour(index++)} );
    }

    var targetPageViews = this.getCampaignTargetPageViews();
    if (targetPageViews) {
      data.push( {name: "target page views", count: targetPageViews, fill: getFillColour(index++)} );
    }

    var targetPageViewsToDate = this.getPageViewsTargetToDate();
    if (targetPageViewsToDate) {
      data.push( {name: "target page views to date", count: targetPageViewsToDate, fill: getFillColour(index++)} );
    }

    var pageViews = this.getPageViews();
    if (pageViews) {
      data.push( {name: "page views", count: pageViews, fill: getFillColour(index++)} );
    }

    return data;
  }

  render () {

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Campaign performance</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <RadialBarChart cx={250} cy={250} innerRadius={20} outerRadius={240} barSize={10} data={this.buildContributionData()}>
              <RadialBar minAngle={5} label background clockWise={true} dataKey='count'/>
              <Legend iconSize={10} layout='horizontal' align='center' verticalAlign='bottom'/>
              <Tooltip />
            </RadialBarChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignPerformanceSummary;

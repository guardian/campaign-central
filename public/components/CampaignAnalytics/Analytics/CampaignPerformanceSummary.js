import React, { PropTypes } from 'react'
import {RadialBarChart, RadialBar, Legend, ResponsiveContainer} from 'recharts'
import {formatPath, getFillColour} from '../../../util/analyticsHelper'
import {pageCountStatPropType} from '../../../propTypes/analytics'

class CampaignPerformanceSummary extends React.Component {

  static propTypes = {
    latestCounts: pageCountStatPropType.isRequired,
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

  buildContributionData() {
    var index = 0;
    var data = [];

    var target = this.getCampaignTargetUniques();
    if (target) {
      data.push( {name: "target", count: target, fill: getFillColour(index++)} );
    }

    data.push( {name: "uniques", count: this.props.latestCounts["cumulative-unique-total"], fill: getFillColour(index++)} );

    for(var i = 0; i < this.props.paths.length; i++) {
      const path = this.props.paths[i];
      data.push( {name: formatPath(path), count: this.props.latestCounts["cumulative-unique" + path], fill: getFillColour(index++)} );
    }

    return data;
  }

  render () {

    return (
      <div className="analytics-chart--full-width">
        <div className="campaign-box__header">Campaign performance</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <RadialBarChart cx={250} cy={250} innerRadius={20} outerRadius={240} barSize={10} data={this.buildContributionData()}>
              <RadialBar minAngle={15} label background clockWise={true} dataKey='count'/>
              <Legend iconSize={10} layout='vertical' align='right' verticalAlign='top'/>
            </RadialBarChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignPerformanceSummary;
import React, { PropTypes } from 'react'
import {RadialBarChart, RadialBar, Legend, ResponsiveContainer} from 'recharts'
import {analyticsPalette} from '../../../constants/analyticsPalette'

class CampaignPerformanceSummary extends React.Component {

  getColour(index) {
    const i = index % analyticsPalette.length;
    return analyticsPalette[i].fill;
  }

  formatPath(p) {
    var pathParts = p.split('/');
    return pathParts[pathParts.length - 1];
  }

  getCampaignTargetUniques() {
    var targets = this.props.campaign.targets;

    for(var i = 0; i < targets.length; i++) {
      if(targets[i].targetType === "Unique users"){
        return targets[i].value;
      }
    }
    return undefined;
  }

  buildContributionData() {
    var index = 0;
    var data = [];

    var target = this.getCampaignTargetUniques();
    if (target) {
      data.push( {name: "target", count: target, fill: this.getColour(index++)} );
    }

    data.push( {name: "uniques", count: this.props.latestCounts["cumulative-unique-total"], fill: this.getColour(index++)} )

    for(var i = 0; i < this.props.paths.length; i++) {
      const path = this.props.paths[i];
      data.push( {name: this.formatPath(path), count: this.props.latestCounts["cumulative-unique" + path], fill: this.getColour(index++)} );
    }

    return data;
  }

  render () {

    return (
      <div className="analytics-chart__full-width">
        <label className="analytics-chart__label">Campaign performance</label>
        <ResponsiveContainer height={300} width="90%">
          <RadialBarChart cx={250} cy={250} innerRadius={20} outerRadius={240} barSize={10} data={this.buildContributionData()}>
            <RadialBar minAngle={15} label background clockWise={true} dataKey='count'/>
            <Legend iconSize={10} width={120} height={140} layout='vertical' align='right' verticalAlign='middle'/>
          </RadialBarChart>
        </ResponsiveContainer>
      </div>
    );
  }
}

export default CampaignPerformanceSummary;
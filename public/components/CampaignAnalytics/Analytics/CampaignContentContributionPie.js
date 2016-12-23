import React, { PropTypes } from 'react'
import {PieChart, Pie, Legend, ResponsiveContainer, Tooltip} from 'recharts'
import {formatPath, getFillColour} from '../../../util/analyticsHelper'

class CampaignContentContributionPie extends React.Component {


  buildContributionData() {
    var index = 0;
    var data = [];

    for(var i = 0; i < this.props.paths.length; i++) {
      const path = this.props.paths[i];
      data.push({
        name: formatPath(path),
        value: this.props.latestPageViews["cumulative-unique" + path],
        fill: getFillColour(index++)
      });
    }

    return data;
  }

  render () {

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Content contribution</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <PieChart >
              <Pie data={this.buildContributionData()} label />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignContentContributionPie;

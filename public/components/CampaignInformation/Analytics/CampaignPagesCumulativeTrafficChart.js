import React, { PropTypes } from 'react'
import {AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {analyticsPalette} from '../../../constants/analyticsPalette'

class CampaignPagesCumulativeTrafficChart extends React.Component {


  getStrokeColour(index) {
    const i = index % analyticsPalette.length;
    return analyticsPalette[i].stroke;
  }
  
  getFillColour(index) {
    const i = index % analyticsPalette.length;
    return analyticsPalette[i].fill;
  }

  formatDate(millis) {
    const date = new Date(millis);
    return date.toLocaleDateString();
  }

  tooltipFormatDate(millis) {
    var date = new Date(millis);
    return date.toDateString();
  }

  formatPath(p) {
    var pathParts = p.split('/');
    return pathParts[pathParts.length - 1];
  }

  render () {

    return (
      <div className="analytics-chart">
        <label>Cumulative uniques</label>
        <ResponsiveContainer height="300" width="90%">
          <AreaChart data={this.props.pageCountStats}>
            <XAxis dataKey="date" tickFormatter={this.formatDate} label="Date" />
            <YAxis label="Views"/>
            <Tooltip labelFormatter={this.tooltipFormatDate} />
            <Legend />
            {this.props.paths.map((p, index) =>
              <Area key={index}
                    type='linear'
                    dataKey={'cumulative-unique' + p}
                    stackId="1"
                    name={this.formatPath(p)}
                    stroke={this.getStrokeColour(index)}
                    fill={this.getFillColour(index)} />
            )}
          </AreaChart>
        </ResponsiveContainer>
      </div>
    );
  }
}

export default CampaignPagesCumulativeTrafficChart;
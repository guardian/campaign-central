import React, { PropTypes } from 'react'
import {AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {analyticsPalette} from '../../../constants/analyticsPalette'
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter'

class CampaignPagesCumulativeTrafficChart extends React.Component {


  getStrokeColour(index) {
    const i = index % analyticsPalette.length;
    return analyticsPalette[i].stroke;
  }
  
  getFillColour(index) {
    const i = index % analyticsPalette.length;
    return analyticsPalette[i].fill;
  }

  formatPath(p) {
    var pathParts = p.split('/');
    return pathParts[pathParts.length - 1];
  }

  render () {

    return (
      <div className="analytics-chart--half-width">
        <label className="analytics-chart__label">Cumulative uniques</label>
        <ResponsiveContainer height={300} width="90%">
          <AreaChart data={this.props.pageCountStats}>
            <XAxis dataKey="date" tickFormatter={shortFormatMillisecondDate} label="Date" />
            <YAxis label="Views"/>
            <Tooltip labelFormatter={formatMillisecondDate} />
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
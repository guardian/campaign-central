import React, { PropTypes } from 'react'
import {AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'

class CampaignPagesCumulativeTrafficChart extends React.Component {

  formatDate(millis) {
    const date = new Date(millis);
    return date.toLocaleDateString();
  }

  tooltipFormatDate(millis) {
    var date = new Date(millis);
    return date.toDateString();
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
            {this.props.paths.map((p) => <Area key={p} type='linear' dataKey={'cumulative-unique' + p} stackId="1" stroke='#8884d8' fill='#8884d8' />)}
          </AreaChart>
        </ResponsiveContainer>
      </div>
    );
  }
}

export default CampaignPagesCumulativeTrafficChart;
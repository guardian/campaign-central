import React, { PropTypes } from 'react'
import {LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'

class CampaignDailyTrafficChart extends React.Component {

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
        <label>Daily page views</label>
        <ResponsiveContainer height={300} width="90%">
          <LineChart data={this.props.pageCountStats}>
            <XAxis dataKey="date" tickFormatter={this.formatDate} label="Date" />
            <YAxis label="Views"/>
            <Line type="linear" dataKey="count-total" stroke="#8884d8"  name="Page Views"/>
            <Line type="linear" dataKey="unique-total" stroke="#82ca9d" name="Uniques"/>
            <Tooltip labelFormatter={this.tooltipFormatDate} />
            <Legend />
          </LineChart>
        </ResponsiveContainer>
      </div>
    );
  }
}

export default CampaignDailyTrafficChart;
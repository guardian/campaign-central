import React, { PropTypes } from 'react'
import {LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter'

class CampaignDailyTrafficChart extends React.Component {

  render () {

    return (
      <div className="analytics-chart--half-width">
        <label className="analytics-chart__label">Daily page views</label>
        <ResponsiveContainer height={300} width="90%">
          <LineChart data={this.props.pageCountStats}>
            <XAxis dataKey="date" tickFormatter={shortFormatMillisecondDate} label="Date" />
            <YAxis label="Views"/>
            <Line type="linear" dataKey="count-total" stroke="#8884d8"  name="Page Views"/>
            <Line type="linear" dataKey="unique-total" stroke="#82ca9d" name="Uniques"/>
            <Tooltip labelFormatter={formatMillisecondDate} />
            <Legend />
          </LineChart>
        </ResponsiveContainer>
      </div>
    );
  }
}

export default CampaignDailyTrafficChart;
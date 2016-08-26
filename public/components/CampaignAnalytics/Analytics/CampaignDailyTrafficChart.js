import React, { PropTypes } from 'react'
import {LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter'
import {getStrokeColour} from '../../../util/analyticsHelper'

class CampaignDailyTrafficChart extends React.Component {

  render () {

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Daily page views</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <LineChart data={this.props.pageCountStats}>
              <XAxis dataKey="date" tickFormatter={shortFormatMillisecondDate} label="Date" />
              <YAxis label="Views"/>
              <Line type="linear" dataKey="count-total" stroke={getStrokeColour(0)}  name="Page Views"/>
              <Line type="linear" dataKey="unique-total" stroke={getStrokeColour(1)} name="Uniques"/>
              <Tooltip labelFormatter={formatMillisecondDate} />
              <Legend />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignDailyTrafficChart;
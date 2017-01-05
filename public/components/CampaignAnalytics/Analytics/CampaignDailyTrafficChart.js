import React, { PropTypes } from 'react'
import {LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter'
import {getStrokeColour} from '../../../util/analyticsHelper'
import {pageCountStatPropType} from '../../../propTypes/analytics'

class CampaignDailyTrafficChart extends React.Component {

  static propTypes = {
    pageCountStats: PropTypes.arrayOf(pageCountStatPropType).isRequired
  };

  combineData() {
    if (this.props.dailyUniques) {
      return this.props.pageCountStats.map((p, index) => Object.assign(p, this.props.dailyUniques[index]));
    }

    return this.props.pageCountStats
  }

  render () {

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Daily page views</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <LineChart data={this.combineData()}>
              <XAxis dataKey="date" tickFormatter={shortFormatMillisecondDate} label="Date" />
              <YAxis label="Views"/>
              <Line type="linear" dataKey="count-total" stroke={getStrokeColour(0)}  name="Page views" dot={false}/>
              <Line type="linear" dataKey="unique-total" stroke={getStrokeColour(1)} name="Unique views" dot={false} />
              <Line type="linear" dataKey="uniqueUsers" stroke={getStrokeColour(3)} name="Estimated unique users" dot={false} />
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

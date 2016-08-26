import React, { PropTypes } from 'react'
import {AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter'
import {formatPath, getStrokeColour, getFillColour} from '../../../util/analyticsHelper'
import {pageCountStat} from '../../../propTypes/analytics'

class CampaignPagesCumulativeTrafficChart extends React.Component {

  static propTypes = {
    pageCountStats: PropTypes.arrayOf(pageCountStat).isRequired,
    paths: PropTypes.arrayOf(PropTypes.string).isRequired
  };

  render () {

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Cumulative uniques</div>
        <div className="campaign-box__body">
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
                      name={formatPath(p)}
                      stroke={getStrokeColour(index)}
                      fill={getFillColour(index)} />
              )}
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignPagesCumulativeTrafficChart;
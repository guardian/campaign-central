import React, { PropTypes } from 'react'
import {ComposedChart, Area, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter'
import {formatPath, getStrokeColour, getFillColour} from '../../../util/analyticsHelper'
import {pageCountStatPropType} from '../../../propTypes/analytics'

class CampaignPagesCumulativeTrafficChart extends React.Component {

  static propTypes = {
    pageCountStats: PropTypes.arrayOf(pageCountStatPropType).isRequired,
    paths: PropTypes.arrayOf(PropTypes.string).isRequired
  };

  render () {

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Cumulative uniques</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <ComposedChart data={this.props.pageCountStats}>
              <XAxis dataKey="date" tickFormatter={shortFormatMillisecondDate} label="Date" />
              <YAxis label="Views"/>
              <Tooltip labelFormatter={formatMillisecondDate} />
              <Legend />
              <Line type="linear" dataKey="cumulative-target-uniques" stroke={getStrokeColour(0)} name="Target uniques" dot={false}/>
              {this.props.paths.map((p, index) =>
                <Area key={index}
                      type='linear'
                      dataKey={'cumulative-unique' + p}
                      stackId="1"
                      name={formatPath(p)}
                      stroke={getStrokeColour(index + 1)}
                      fill={getFillColour(index + 1 )} />
              )}
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignPagesCumulativeTrafficChart;

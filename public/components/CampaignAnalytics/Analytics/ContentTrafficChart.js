import React, { PropTypes } from 'react'
import {ComposedChart, Line, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter'
import {formatPath, getStrokeColour, getFillColour} from '../../../util/analyticsHelper'

class ContentTrafficChart extends React.Component {
  
  render () {

    return (
      <div className="analytics-chart--third-width">
        <div className="campaign-box__header">Content views for {formatPath(this.props.path)}</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <ComposedChart data={this.props.pageCountStats}>
              <XAxis dataKey="date" tickFormatter={shortFormatMillisecondDate} label="Date" />
              <YAxis label="Views"/>
              <Line type="linear" dataKey={"count" + this.props.path} stroke={getStrokeColour(0)} name="Daily page Views" dot={false} />
              <Line type="linear" dataKey={"unique" + this.props.path} stroke={getStrokeColour(1)} name="Daily uniques" dot={false}/>
              <Area type='linear'
                    dataKey={'cumulative-count' + this.props.path}
                    stackId="0"
                    name="Cumulative page views"
                    stroke={getStrokeColour(0)}
                    fill={getFillColour(0)} />
              <Area type='linear'
                    dataKey={'cumulative-unique' + this.props.path}
                    stackId="1"
                    name="Cumulative uniques"
                    stroke={getStrokeColour(1)}
                    fill={getFillColour(1)} />
              <Tooltip labelFormatter={formatMillisecondDate} />
              <Legend />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default ContentTrafficChart;
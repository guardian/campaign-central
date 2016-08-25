import React, { PropTypes } from 'react'
import {ComposedChart, Line, Area, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {analyticsPalette} from '../../../constants/analyticsPalette'

class ContentTrafficChart extends React.Component {

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
      <div className="analytics-chart__quarter-width">
        <label className="analytics-chart__label">content views for {this.formatPath(this.props.path)}</label>
        <ResponsiveContainer height={300} width="90%">
          <ComposedChart data={this.props.pageCountStats}>
            <XAxis dataKey="date" tickFormatter={this.formatDate} label="Date" />
            <YAxis label="Views"/>
            <Line type="linear" dataKey={"count" + this.props.path} stroke={this.getStrokeColour(0)} name="Daily page Views" dot={false} />
            <Line type="linear" dataKey={"unique" + this.props.path} stroke={this.getStrokeColour(1)} name="Daily uniques" dot={false}/>
            <Area type='linear'
                  dataKey={'cumulative-count' + this.props.path}
                  stackId="0"
                  name="Cumulative page views"
                  stroke={this.getStrokeColour(0)}
                  fill={this.getFillColour(0)} />
            <Area type='linear'
                  dataKey={'cumulative-unique' + this.props.path}
                  stackId="1"
                  name="Cumulative uniques"
                  stroke={this.getStrokeColour(1)}
                  fill={this.getFillColour(1)} />
            <Tooltip labelFormatter={this.tooltipFormatDate} />
            <Legend />
          </ComposedChart>
        </ResponsiveContainer>
      </div>
    );
  }
}

export default ContentTrafficChart;
import React, { PropTypes } from 'react'
import {LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts'
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter'
import {formatPath, getStrokeColour, getFillColour} from '../../../util/analyticsHelper'

class CampaignPagesCumulativeTrafficChart extends React.Component {


  combineData() {
    if (this.props.targets && this.props.targets.targets.uniques) {
      const targetRunRate = this.props.targets.targets.uniques.runRate;
      var combined = this.props.dailyUniques.map((p, index) => Object.assign(p, targetRunRate[index]));
      console.log(combined);
      return combined;
    }

    return this.props.dailyUniques
  }

  render () {

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Estimated uniques</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <LineChart data={this.combineData()}>
              <XAxis dataKey="date" tickFormatter={shortFormatMillisecondDate} label="Date" />
              <YAxis label="Views"/>
              <Line type="linear" dataKey="cumulativeUniqueUsers" stroke={getStrokeColour(1)} name="Estimated uniques" dot={false}/>
              <Line type="linear" dataKey="expected" stroke={getStrokeColour(2)} name="Target uniques" dot={false}/>
              <Tooltip labelFormatter={formatMillisecondDate} />
              <Legend />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignPagesCumulativeTrafficChart;

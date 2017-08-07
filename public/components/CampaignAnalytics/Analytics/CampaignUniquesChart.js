import React, { PropTypes } from 'react';
import Moment from 'moment';
import {LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer} from 'recharts';
import {formatMillisecondDate, shortFormatMillisecondDate} from '../../../util/dateFormatter';
import {getStrokeColour} from '../../../util/analyticsHelper';

export default class CampaignUniquesChart extends React.Component {


  render () {

    if (!this.props.data || !this.props.data.length > 0) {
      return null;
    }

    const data = this.props.data.map((item) => {
      return {
        name: Moment(item.name).format("YYYY-MM-DD"),
        uniques: item.dataPoint,
        target: item.target
      }
    });

    return(
      <div className="analytics-chart">
        <div className="campaign-box__header">Campaign performance</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="100%">
            <LineChart width={600} height={300} data={data}
                       margin={{top: 40, right: 40, left: 20, bottom: 5}}>
              <XAxis dataKey="name" tickFormatter={shortFormatMillisecondDate} label="Date"/>
              <YAxis label="Views"/>
              <CartesianGrid strokeDasharray="3 3"/>
              <Tooltip labelFormatter={formatMillisecondDate}/>
              <Legend />
              <Line type="monotone" dataKey="uniques" stroke={getStrokeColour(2)} />
              <Line type="monotone" dataKey="target" stroke={getStrokeColour(1)} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

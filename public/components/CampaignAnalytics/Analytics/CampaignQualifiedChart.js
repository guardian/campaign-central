import React, { PropTypes } from 'react'
import {PieChart, Pie, Legend, ResponsiveContainer, Tooltip} from 'recharts'

class CampaignQualifiedChart extends React.Component {


  render () {

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Qualified views</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
          <p>A chart of qualified view data will be added here soon.</p>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignQualifiedChart;

import React, { PropTypes } from 'react'
import LineChart from 'react-d3-basic'

class CampaignDailyTrafficChart extends React.Component {

  render () {

    const chartSeries = [{field: 'count-total', name: 'Page Views'},{field: 'unique-total', name: 'Uniques'}];

    const xAccessor = function(d) {
      return d.date;
    };

    return (
      <LineChart
        title='Daily traffic - all content'
        data={this.props.pageCountStats}
        chartSeries={chartSeries}
        x={xAccessor}
        xScale='time'
      />
    );
  }
}

export default CampaignDailyTrafficChart;
import React, { PropTypes } from 'react'
import {BarChart, Bar, XAxis, Legend, ResponsiveContainer, Tooltip} from 'recharts'
import {getFillColour} from '../../../util/analyticsHelper'

class CampaignQualifiedChart extends React.Component {

  buildQualifiedData() {
    var data = [];
    var index = 0;

    var articleDwellTime = this.props.qualifiedReport.metrics.articleDwellTime;
    if (articleDwellTime) {
      data.push( {name: "article", percentage: articleDwellTime.percentage, fill: getFillColour(index++) } );
    }

    var galleryDwellTime = this.props.qualifiedReport.metrics.galleryDwellTime;
    if (galleryDwellTime) {
      data.push( {name: "gallery", percentage: galleryDwellTime.percentage, fill: getFillColour(index++) } );
    }

    var interactiveDwellTime = this.props.qualifiedReport.metrics.interactiveDwellTime;
    if (interactiveDwellTime) {
      data.push( {name: "interactive", percentage: interactiveDwellTime.percentage, fill: getFillColour(index++) } );
    }

    var videoPlays = this.props.qualifiedReport.metrics.videoPlays;
    if (videoPlays) {
      data.push( {name: "video starts", percentage: videoPlays.percentage, fill: getFillColour(index)} );
    }

    var video25Percent = this.props.qualifiedReport.metrics.video25Percent;
    if (video25Percent) {
      data.push( {name: "video 25%", percentage: video25Percent.percentage, fill: getFillColour(index)} );
    }

    var video50Percent = this.props.qualifiedReport.metrics.video50Percent;
    if (video50Percent) {
      data.push( {name: "video 50%", percentage: video50Percent.percentage, fill: getFillColour(index) } );
    }

    var video75Percent = this.props.qualifiedReport.metrics.video75Percent;
    if (videoPlays) {
      data.push( {name: "video 75%", percentage: video75Percent.percentage, fill: getFillColour(index) } );
    }

    var videoComplete = this.props.qualifiedReport.metrics.videoComplete;
    if (videoPlays) {
      data.push( {name: "video complete", percentage: videoComplete.percentage, fill: getFillColour(index) } );
    }

    return data;
  }

  formatPercent(p) {
    return parseFloat(p).toFixed(2) + '%';
  }

  render () {

    if ( !this.props.qualifiedReport || !this.props.qualifiedReport.metrics) {
      return (
        <div className="analytics-chart--half-width">
          <div className="campaign-box__header">Qualified views</div>
          <div className="campaign-box__body">
            <ResponsiveContainer height={300} width="90%">
              <p>Qualified views are not available for this campaign</p>
            </ResponsiveContainer>
          </div>
        </div>
      );
    }

    return (
      <div className="analytics-chart--half-width">
        <div className="campaign-box__header">Qualified views</div>
        <div className="campaign-box__body">
          <ResponsiveContainer height={300} width="90%">
            <BarChart data={this.buildQualifiedData()}>
              <Bar minAngle={5} dataKey='percentage'/>
              <XAxis dataKey="name"/>
              <Tooltip formatter={this.formatPercent} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    );
  }
}

export default CampaignQualifiedChart;

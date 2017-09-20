import React from 'react';

const MedianAttentionTimeExplainerText = 'Attention time is the measure of how long a user is knowingly active on the page; they have recently moved their mouse or scrolled within 5 seconds, or are watching a video in a focused tab.';
const DwellTimeExplainerText = 'Dwell time tries to emulate the same viewability metric from Google Analytics. It is calculated by taking the difference between a pageview timestamp and the subsequent pageview timestamp. These pageviews have to happen within a 30 minute session period, and are averaged when there has been more than one pageview for a unique user.';

class Glossary extends React.Component {

  render() {
    return (
      <div className="glossary">
        <h1>Median Attention Time</h1>
        <p>{MedianAttentionTimeExplainerText}</p>
        <h1>Average Dwell Time</h1>
        <p>{DwellTimeExplainerText}</p>
      </div>
    );
  }
}

export { Glossary, DwellTimeExplainerText, MedianAttentionTimeExplainerText };

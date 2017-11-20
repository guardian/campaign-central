import React from 'react';

export default class CampaignMediaEvents extends React.Component {

  constructor(props) {
    super(props);
  }

  render () {
    if (!this.props.mediaEventsData || !this.props.mediaEventsData.pages  ) {
        return null;
    }
    return (
      <div className="campaign__row">
        <div className="campaign-box__header">Campaign media events</div>
        <div className="campaign-box__body">
          <div className="campaign-assets__field__value">
              <table className="campaign-media__table pure-table">
                <thead>
                  <tr>
                    <th className="campaign-media__table--header campaign-media__table--header--primary">Path</th>
                    <th className="campaign-media__table--header campaign-media__table--header--secondary">Play</th>
                    <th className="campaign-media__table--header campaign-media__table--header--secondary">25%</th>
                    <th className="campaign-media__table--header campaign-media__table--header--secondary">50%</th>
                    <th className="campaign-media__table--header campaign-media__table--header--secondary">75%</th>
                    <th className="campaign-media__table--header campaign-media__table--header--secondary">End</th>
                    <th className="campaign-media__table--header campaign-media__table--header--tertiary">View-through rate</th>
                  </tr>
                </thead>

                <tbody>
                {this.props.mediaEventsData.pages.map((
                    {   endEvent,
                        path,
                        percent25,
                        percent50,
                        percent75,
                        playEvent
                    }) => {

                  const index = path.lastIndexOf("/");
                  const lastSlug = path.substr(index + 1);

                  return(
                    <tr key={lastSlug}>
                      <td>{lastSlug}</td>
                      <td>{`${playEvent.toLocaleString()}`}</td>
                      <td>{`${percent25.toLocaleString()}`}</td>
                      <td>{`${percent50.toLocaleString()}`}</td>
                      <td>{`${percent75.toLocaleString()}`}</td>
                      <td>{`${endEvent.toLocaleString()}`}</td>
                      <td>{`${(100 * endEvent / playEvent).toFixed(2).toLocaleString()}%`}</td>
                    </tr>
                  );

                })}
                </tbody>
              </table>
          </div>
        </div>
      </div>
    );
  }
}

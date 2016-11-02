import React, {Component, PropTypes} from 'react';
import {Link} from 'react-router';
import {fetchAnalyticsCacheSummary, refreshItem, deleteItem} from '../../services/ManagementApi';
import {isoFormatMillisecondDate} from '../../util/dateFormatter'

class AnalyticsCache extends Component {

  constructor(props) {
    super(props);
    this.state = {
      cacheState: []
    };
  };

  componentWillMount() {
    this.loadAnalyticsCacheState();
  }

  loadAnalyticsCacheState() {
    fetchAnalyticsCacheSummary().then((cacheState) => {
      this.setState({
        cacheState: cacheState
      })
    });
  }

  renderExpiry = (item) => {
    if (item.expires) {
      if (item.expires < Date.now()) {
        return (
          <div className="analytics-cache-list__expires--expired">
            {isoFormatMillisecondDate(item.expires)}
            <i className="i-delete campaign-box__header__refresh-asset-button" onClick={() => deleteItem(item)} />
            <i className="i-refresh-black campaign-box__header__refresh-asset-button" onClick={() => refreshItem(item)} />
          </div>
        )
      }
      return (
        <div className="analytics-cache-list__expires">
          {isoFormatMillisecondDate(item.expires)}
          <i className="i-delete campaign-box__header__refresh-asset-button" onClick={() => deleteItem(item)} />
        </div>
      )
    }

    return (
      <div className="analytics-cache-list__expires">
        never
        <i className="i-delete campaign-box__header__refresh-asset-button" onClick={() => deleteItem(item)} />
      </div>
    )
  }

  renderCacheItem = (item) => {

    return (
      <div key={item.key + item.dateType} className="analytics-cache-list__item">
        <div className="analytics-cache-list__row">
          <div className="analytics-cache-list__key">
            <Link to={"/campaign/" + item.key}>{item.key}</Link>
          </div>
          <div className="analytics-cache-list__type">{item.dataType}</div>
          <div className="analytics-cache-list__written">{isoFormatMillisecondDate(item.written)}</div>
          {this.renderExpiry(item)}
        </div>
      </div>
    );
  }

  renderCacheItems = () => {

    if(this.state.cacheState.length > 0) {
      return (
        <div className="analytics-cache-list">
          <div className="analytics-cache-list__row">
            <div className="analytics-cache-list__key--header">Campaign id</div>
            <div className="analytics-cache-list__type--header">Data type</div>
            <div className="analytics-cache-list__written--header">Written</div>
            <div className="analytics-cache-list__expires--header">Expires</div>
          </div>
          {this.state.cacheState.map( this.renderCacheItem ) }
        </div>
      );
    }

    return (
      <span className="campaign-assets__field__value">
        Fetching cache data (or cache is empty)
      </span>
    )
  }

  render() {

    return (
      <div className="campaigns">
        <h2 className="campaigns__header">analytics cache management</h2>
        {this.renderCacheItems()}
      </div>
    );
  }
}

export default AnalyticsCache;

import 'babel-polyfill';
import React from 'react';
import {render} from 'react-dom';
import {Provider} from 'react-redux';

import configureStore from './util/configureStore';
import {setStore} from './util/storeAccessor';
import {router} from './router';
import ReactGA from 'react-ga';
import { browserHistory } from './util/browserHistory'

import './styles/main.scss';

function extractConfigFromPage() {

  const configEl = document.getElementById('config');

  if (!configEl) {
    return {};
  }

  return JSON.parse(configEl.innerHTML);
}

const store = configureStore();
const config = extractConfigFromPage();

setStore(store);

store.dispatch({
    type:       'CONFIG_RECEIVED',
    config:     config,
    receivedAt: Date.now()
});


render(
    <Provider store={store}>
      {router}
    </Provider>
, document.getElementById('react-mount')
);


const devEnv = config.stage === 'DEV';

const trackPageView = location => {
  ReactGA.pageview(location.pathname + location.search);
};

const initGa = history => {
  ReactGA.initialize(config.gaTrackingCode);
  if (devEnv) {
    // see https://developers.google.com/analytics/devguides/collection/analyticsjs/debugging#testing_your_implementation_without_sending_hits
    ReactGA.ga('set', 'sendHitTask', null);
  }
  history.listen(trackPageView);
  trackPageView(window.location);
};

initGa(browserHistory);

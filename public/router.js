import React from 'react';
import {Router, Route, IndexRoute, browserHistory, IndexRedirect} from 'react-router';

import Main from './components/Main';
import Campaigns from './components/Campaigns/Campaigns';
import Campaign from './components/Campaign/Campaign';

import CapiImport from './components/CapiImport/CapiImport';
import AnalyticsCache from './components/Management/AnalyticsCache';

import { Glossary } from './components/Glossary/Glossary';

export const router = (
  <Router history={browserHistory}>
    <Route path="/" component={Main}>
      <IndexRedirect to="/campaigns" />
      <Route path="/campaigns" component={Campaigns} />
      <Route path="/campaigns/:filterName" component={Campaigns} />
      <Route path="/campaign/:id" component={Campaign} />
      <Route path="/capiImport" component={CapiImport} />
      <Route path="/management/analytics" component={AnalyticsCache} />
      <Route path="/glossary" component={Glossary} />
    </Route>
  </Router>
);

import React from 'react';
import {Router, Route, IndexRoute, browserHistory, IndexRedirect} from 'react-router';

import {getStore} from './util/storeAccessor';
import {hasPermission} from './util/verifyPermission';

import Main from './components/Main';
import Campaigns from './components/Campaigns/Campaigns';
import Campaign from './components/Campaign/Campaign';

import Clients from './components/Clients/Clients';
import Client from './components/Client/Client';

import CapiImport from './components/CapiImport/CapiImport';

function requirePermission(permissionName, nextState, replaceState) {
  if (!hasPermission(permissionName)) {
    replaceState(null, '/unauthorised');
  }
}

export const router = (
  <Router history={browserHistory}>
    <Route path="/" component={Main}>
      <IndexRedirect to="/campaigns" />
      <Route path="/campaigns" component={Campaigns} />
      <Route path="/campaigns/:filterName" component={Campaigns} />
      <Route path="/campaign/:id" component={Campaign} />
      <Route path="/clients" component={Clients} />
      <Route path="/clients/:id" component={Client} />
      <Route path="/capiImport" component={CapiImport} />
    </Route>
  </Router>
);

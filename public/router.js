import React from 'react';
import {Router, Route, IndexRoute, browserHistory} from 'react-router';

import {getStore} from './util/storeAccessor';
import {hasPermission} from './util/verifyPermission';

import Main from './components/Main';
import Campaigns from './components/Campaigns/Campaigns';

function requirePermission(permissionName, nextState, replaceState) {
  if (!hasPermission(permissionName)) {
    replaceState(null, '/unauthorised');
  }
}

export const router = (
  <Router history={browserHistory}>
    <Route path="/" component={Main}>
      <IndexRoute component={Campaigns} />
    </Route>
  </Router>
)

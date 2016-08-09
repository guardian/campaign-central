import React from 'react';
import {Router, Route, IndexRoute, browserHistory} from 'react-router';

import {getStore} from './util/storeAccessor';
import {hasPermission} from './util/verifyPermission';

import Main from './components/Main';

function requirePermission(permissionName, nextState, replaceState) {
  if (!hasPermission(permissionName)) {
    replaceState(null, '/unauthorised');
  }
}

export const router = (
  <Router history={browserHistory}>
    <Route path="/" component={Main}>
    </Route>
  </Router>
)

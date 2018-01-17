import React from 'react';
import { Router } from 'react-router'
import {Route, Redirect} from 'react-router-dom';

import { browserHistory } from './util/browserHistory';
import Main from './components/Main';

export const router = (
  <Router history={browserHistory}>
      <div>
        <Route path="/" component={ Main }/>
        <Route exact path="/" component={() => <Redirect to="/campaigns"/>}/>
      </div>
  </Router>
);

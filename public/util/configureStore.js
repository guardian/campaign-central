import { compose, createStore, applyMiddleware } from 'redux';
import thunkMiddleware from 'redux-thunk';
//import { devTools, persistState } from 'redux-devtools';
import createLogger from 'redux-logger';

const logger = createLogger({
  level: 'info',
  collapsed: true
});

import rootReducer from '../reducers/rootReducer';

const createStoreWithMiddleware = compose(
  applyMiddleware(
    thunkMiddleware,
    logger
  )
)(createStore);

export default function configureStore(initialState) {
  return createStoreWithMiddleware(rootReducer, initialState);
}

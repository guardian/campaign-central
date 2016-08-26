import { PropTypes } from 'react'

export const pageCountStat = PropTypes.shape({
  date: PropTypes.number,
  "count-total": PropTypes.number,
  "count-total": PropTypes.number,
  "unique-total": PropTypes.number,
  "cumulative-count-total": PropTypes.number,
  "cumulative-unique-total": PropTypes.number
  // NB there will also be a slew of other keys corresponding to individual paths in the campaign
  // these will be named:
  //
  // count{path},
  // uniques{path},
  // cumulative-count{path}
  // cumulative-uniques{path}
  //
  // the available values for the {path}s are provided with the stats data api result as an array of strings called seenPaths
});
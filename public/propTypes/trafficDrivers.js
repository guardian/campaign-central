import {PropTypes} from "react";

export const trafficDriverStatPropType = PropTypes.shape({
  date: PropTypes.number,
  "impressions": PropTypes.number,
  "clicks": PropTypes.number,
  "ctr": PropTypes.number
});

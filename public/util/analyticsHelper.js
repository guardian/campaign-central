import {analyticsPalette} from '../constants/analyticsPalette';

export function getStrokeColour(index) {
  const i = index % analyticsPalette.length;
  return analyticsPalette[i].stroke;
}

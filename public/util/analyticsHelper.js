import {analyticsPalette} from '../constants/analyticsPalette';

export function getStrokeColour(index) {
  const i = index % analyticsPalette.length;
  return analyticsPalette[i].stroke;
}

export function getFillColour(index) {
  const i = index % analyticsPalette.length;
  return analyticsPalette[i].fill;
}

export function formatPath(p) {
  var pathParts = p.split('/');
  return pathParts[pathParts.length - 1];
}

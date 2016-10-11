import {getStore} from './storeAccessor';

export function tagEditUrl(tagId) {
  const tagManagerUrl = getStore().getState().config.tagManagerUrl;
  return tagManagerUrl + '/tag/' + tagId;
}

export function ctaEditUrl(ctaId) {
  const ctaAtomMakerUrl = getStore().getState().config.ctaAtomMakerUrl;
  return ctaAtomMakerUrl + '/#/atom/' + ctaId;
}

export function composerEditUrl(composerId) {
  const composerUrl = getStore().getState().config.composerUrl;
  return composerUrl + '/content/' + composerId;
}

export function liveUrl(path) {
  const liveUrl = getStore().getState().config.liveUrl;
  return liveUrl + '/' + path;
}

export function previewUrl(path) {
  const previewUrl = getStore().getState().config.previewUrl;
  return previewUrl + '/' + path;
}

export function mediaAtomEditUrl(atomId) {
  const mediaAtomMakerUrl = getStore().getState().config.mediaAtomMakerUrl;
  return mediaAtomMakerUrl + '/atoms#/atom/' + atomId;
}

import {getStore} from './storeAccessor';

export function tagEditUrl(tagId) {
  const tagManagerUrl = getStore().getState().config.tagManagerUrl;
  return tagManagerUrl + '/tag/' + tagId;
}

export function ctaEditUrl(ctaId) {
  const ctaAtomMakerUrl = getStore().getState().config.ctaAtomMakerUrl;
  return ctaAtomMakerUrl + '/#/atom/' + ctaId;
}
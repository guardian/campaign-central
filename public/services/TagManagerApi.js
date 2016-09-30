import {getStore} from '../util/storeAccessor'
import {AuthedReqwest} from '../util/pandaReqwest';


export function searchTags(params) {
  const store = getStore();
  const tagsApiUrl = store.getState().config.tagManagerUrl + '/tags';

  return AuthedReqwest({
    url: tagsApiUrl,
    method: 'GET',
    data: params
  });
}
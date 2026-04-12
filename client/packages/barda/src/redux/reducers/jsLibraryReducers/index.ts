import { createReducer } from "util/reducerUtils";
import { JSLibraryMeta } from "api/jsLibraryApi";
import { ReduxAction, ReduxActionTypes } from "constants/reduxActionConstants";

export interface JSLibraryState {
  recommends: JSLibraryMeta[];
  meta: Record<string, JSLibraryMeta>;
}

const initialState: JSLibraryState = {
  recommends: [],
  meta: {},
};

const jsLibraryReducer = createReducer(initialState, {
  [ReduxActionTypes.FETCH_JS_LIB_METAS_SUCCESS]: (
    state: JSLibraryState,
    action: ReduxAction<JSLibraryMeta[]>
  ): JSLibraryState => {
    return {
      ...state,
      meta: {
        ...state.meta,
        ...action.payload.reduce((obj, item) => {
          // 对于内部库，使用downloadUrl作为key；对于外部库，使用name作为key
          const key = item.downloadUrl?.startsWith('/api/libraries/') ? item.downloadUrl : item.name;
          return Object.assign(obj, { [key]: item });
        }, {}),
      },
    };
  },
  [ReduxActionTypes.FETCH_JS_LIB_RECOMMENDS_SUCCESS]: (
    state: JSLibraryState,
    action: ReduxAction<JSLibraryMeta[]>
  ): JSLibraryState => {
    return {
      ...state,
      recommends: action.payload,
    };
  },
});

export default jsLibraryReducer;

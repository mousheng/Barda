import { createReducer } from "util/reducerUtils";
import { BrandingConfig, SystemConfig } from "constants/configConstants";
import {
  ReduxAction,
  ReduxActionErrorTypes,
  ReduxActionTypes,
} from "constants/reduxActionConstants";
import { ExternalEditorContextState } from "util/context/ExternalEditorContext";

const initialState: ConfigState = {
  fetchingConfig: false,
  editorExternalState: {},
};

const configReducer = createReducer(initialState, {
  [ReduxActionTypes.FETCH_SYS_CONFIG_INIT]: (state: ConfigState): ConfigState => ({
    ...state,
    fetchingConfig: true,
  }),
  [ReduxActionTypes.FETCH_SYS_CONFIG_SUCCESS]: (
    state: ConfigState,
    action: ReduxAction<SystemConfig>
  ): ConfigState => {
    return {
      ...state,
      systemConfig: action.payload,
      fetchingConfig: false,
    };
  },
  [ReduxActionErrorTypes.FETCH_SYS_CONFIG_ERROR]: (state: ConfigState): ConfigState => ({
    ...state,
    fetchingConfig: false,
  }),
  [ReduxActionTypes.SET_EDITOR_EXTERNAL_STATE]: (
    state: ConfigState,
    action: ReduxAction<Partial<ExternalEditorContextState>>
  ): ConfigState => {
    return {
      ...state,
      editorExternalState: action.payload,
    };
  },
  [ReduxActionTypes.UPDATE_SYS_CINFIG_BRANDING]: (
    state: ConfigState,
    action: ReduxAction<BrandingConfig>
  ): ConfigState => {
    return {
      ...state,
      systemConfig: {
        ...state.systemConfig,
        branding: action.payload,
      },
    } as ConfigState;
  },
});

export interface ConfigState {
  fetchingConfig: boolean;
  systemConfig?: SystemConfig;
  editorExternalState: Partial<ExternalEditorContextState>;
}

export default configReducer;

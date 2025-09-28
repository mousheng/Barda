package com.barda.plugin.googlesheets;

import com.barda.sdk.exception.PluginError;

public enum GoogleSheetError implements PluginError {

    GOOGLESHEETS_REQUEST_ERROR,
    GOOGLESHEETS_EMPTY_QUERY_PARAM,
    GOOGLESHEETS_EXECUTION_ERROR,

}

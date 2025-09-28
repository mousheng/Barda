package com.barda.plugin.restapi;

import com.barda.sdk.exception.PluginError;

/**
 * 定义了 REST API 相关的错误类型。
 * 实现了 {@link PluginError} 接口，表示这是一个插件相关的错误。
 */
public enum RestApiError implements PluginError {

    /**
     * REST API 执行时发生的错误。
     */
    REST_API_EXECUTION_ERROR,

}

package com.barda.plugin;

import com.barda.sdk.exception.PluginError;

/**
 * BardaApiPluginError 枚举类定义了与 Barda API 插件相关的错误代码。
 * 该枚举类用于表示在与 Barda API 进行交互时可能发生的错误。
 */
public enum BardaApiPluginError implements PluginError {

    /**
     * 指示在与 Barda API 进行请求时发生了错误。
     */
    BARDA_API_REQUEST_ERROR,

    /**
     * 指示在与 Barda API 进行请求时发生了无效的请求类型。
     */
    BARDA_API_INVALID_REQUEST_TYPE,

}

package com.barda.plugin.es;

import com.barda.sdk.exception.PluginError;

/**
 * Elasticsearch相关的错误枚举。
 *
 * <p>
 * 该枚举定义了Elasticsearch执行过程中可能发生的错误。
 * </p>
 */
public enum EsError implements PluginError {

    /**
     * Elasticsearch执行出错。
     *
     * <p>
     * 该错误表示在执行Elasticsearch操作时发生了异常。
     * </p>
     */
    ES_EXECUTION_ERROR,

}

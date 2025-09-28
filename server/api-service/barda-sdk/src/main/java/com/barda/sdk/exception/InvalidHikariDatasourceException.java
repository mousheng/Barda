package com.barda.sdk.exception;

import static com.barda.sdk.exception.PluginCommonError.CONNECTION_ERROR;

/**
 * 自定义的 Hikari 数据库数据源无效异常类。
 * 继承自 {@link PluginException}，用于在 Hikari 数据库数据源关闭时抛出。
 */
public class InvalidHikariDatasourceException extends PluginException {

    /**
     * 构造函数，使用默认的错误代码和消息。
     */
    public InvalidHikariDatasourceException() {
        super(CONNECTION_ERROR, "CONNECTION_ERROR", "hikari datasource closed.");
    }
}

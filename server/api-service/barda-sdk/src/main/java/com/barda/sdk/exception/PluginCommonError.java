package com.barda.sdk.exception;

/**
 * 插件通用错误枚举类。
 * 实现了 {@link PluginError} 接口，用于定义插件中可能发生的通用错误。
 */
public enum PluginCommonError implements PluginError {

    // 通用错误
    QUERY_EXECUTION_ERROR(ErrorLogType.VERBOSE), // 查询执行错误
    QUERY_ARGUMENT_ERROR, // 查询参数错误
    QUERY_EXECUTION_TIMEOUT, // 查询执行超时

    INVALID_QUERY_SETTINGS, // 无效的查询设置
    DATASOURCE_GET_STRUCTURE_ERROR, // 获取数据源结构错误
    DATASOURCE_ARGUMENT_ERROR, // 数据源参数错误
    DATASOURCE_TEST_GENERIC_ERROR(ErrorLogType.VERBOSE), // 通用数据源测试错误
    DATASOURCE_TIMEOUT_ERROR, // 数据源超时错误
    JSON_PARSE_ERROR, // JSON 解析错误

    SQL_IN_OPERATOR_PARSE_ERROR(ErrorLogType.VERBOSE), // SQL IN 运算符解析错误
    PREPARED_STATEMENT_BIND_PARAMETERS_ERROR, // 预编译语句绑定参数错误
    EXCEED_MAX_QUERY_TIMEOUT, // 超出最大查询超时

    CONNECTION_ERROR, // 连接错误

    // SQL GUI 模式错误
    INVALID_GUI_SETTINGS, // 无效的 GUI 设置
    INVALID_INSERT_COMMAND, // 无效的插入命令
    INVALID_UPDATE_COMMAND, // 无效的更新命令
    INVALID_UPSERT_COMMAND, // 无效的 upsert 命令
    INVALID_BULK_INSERT_COMMAND, // 无效的批量插入命令
    INVALID_IN_OPERATOR_SETTINGS, // 无效的 IN 运算符设置
    ;

    private final ErrorLogType logType;

    /**
     * 构造函数，使用指定的 {@link ErrorLogType} 初始化 {@link #logType}。
     *
     * @param logType 错误日志类型
     */
    PluginCommonError(ErrorLogType logType) {
        this.logType = logType;
    }

    /**
     * 无参构造函数，使用默认的 {@link ErrorLogType#SIMPLE} 初始化 {@link #logType}。
     */
    PluginCommonError() {
        this(ErrorLogType.SIMPLE);
    }


    /**
     * 实现 {@link PluginError#logVerbose()} 方法，返回 {@link #logType} 是否为 {@link ErrorLogType#VERBOSE}。
     *
     * @return true 如果 {@link #logType} 为 {@link ErrorLogType#VERBOSE}，否则返回 false
     */
    @Override
    public boolean logVerbose() {
        return logType == ErrorLogType.VERBOSE;
    }
}

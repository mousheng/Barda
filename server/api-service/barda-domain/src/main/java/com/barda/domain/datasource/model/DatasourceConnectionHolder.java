package com.barda.domain.datasource.model;

/**
 * 该接口定义了数据源连接持有者的行为。
 * 它用于管理和操作由数据源插件创建的数据库连接。
 */
public interface DatasourceConnectionHolder {

    /**
     * 返回由数据源插件创建的连接对象。
     *
     * @return 数据库连接对象
     */
    Object connection();

    /**
     * 处理查询时发生的错误。
     *
     * @param throwable 发生的错误
     */
    void onQueryError(Throwable throwable);
}

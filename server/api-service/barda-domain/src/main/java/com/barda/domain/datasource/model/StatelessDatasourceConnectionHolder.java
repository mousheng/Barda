package com.barda.domain.datasource.model;

/**
 * 该类实现了 DatasourceConnectionHolder 接口，用于提供无状态的数据源连接。
 * 它使用一个单例的 CONNECTION 对象来表示数据源的连接。
 */
public class StatelessDatasourceConnectionHolder implements DatasourceConnectionHolder {

    /**
     * 一个单例的 CONNECTION 对象，表示数据源的连接。
     */
    private static final Object CONNECTION = new Object();

    /**
     * 该方法在执行查询时发生错误时被调用。
     * 目前，该方法什么都不做。
     *
     * @param throwable 发生的错误
     */
    @Override
    public void onQueryError(Throwable throwable) {
    }

    /**
     * 获取数据源的连接。
     *
     * @return 数据源的连接
     */
    @Override
    public Object connection() {
        return CONNECTION;
    }
}

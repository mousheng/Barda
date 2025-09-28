package com.barda.api.datasource;

import com.barda.domain.datasource.model.Datasource;

/**
 * 数据源视图。
 */
public record DatasourceView(Datasource datasource, boolean edit, String creatorName) {

    /**
     * 构造一个数据源视图。
     *
     * @param datasource   数据源信息
     * @param edit         是否可编辑
     */
    public DatasourceView(Datasource datasource, boolean edit) {
        this(datasource, edit, null);
    }
}


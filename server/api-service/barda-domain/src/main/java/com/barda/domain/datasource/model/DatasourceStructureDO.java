package com.barda.domain.datasource.model;


import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.models.HasIdAndAuditing;
import com.querydsl.core.annotations.QueryExclude;

/**
 * 该类用于存储数据源的结构信息。
 * 它继承了 HasIdAndAuditing 类，并使用了 @QueryExclude 注解来排除在查询中。
 * 该类的数据存储在 "datasourceStructure" 集合中。
 */
@QueryExclude
@Document(collection = "datasourceStructure")
public class DatasourceStructureDO extends HasIdAndAuditing {

    /**
     * 该属性存储数据源的 ID。
     */
    private String datasourceId;

    /**
     * 该属性存储数据源的结构。
     */
    private DatasourceStructure structure;

    /**
     * 获取数据源 ID。
     *
     * @return 数据源 ID
     */
    public String getDatasourceId() {
        return datasourceId;
    }

    /**
     * 设置数据源 ID。
     *
     * @param datasourceId 要设置的数据源 ID
     */
    public void setDatasourceId(String datasourceId) {
        this.datasourceId = datasourceId;
    }

    /**
     * 获取数据源的结构。
     *
     * @return 数据源的结构
     */
    public DatasourceStructure getStructure() {
        return structure;
    }

    /**
     * 设置数据源的结构。
     *
     * @param structure 要设置的数据源的结构
     */
    public void setStructure(DatasourceStructure structure) {
        this.structure = structure;
    }
}

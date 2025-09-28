package com.barda.domain.datasource.model;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.HasIdAndAuditing;
import com.querydsl.core.annotations.QueryExclude;

import lombok.Getter;
import lombok.Setter;

/**
 * 该类表示数据源的领域对象 (DO)。
 * 它包含数据源的基本信息，并存储在 MongoDB 集合中。
 */
@QueryExclude
@Document(collection = "datasource")
@Getter
@Setter
public class DatasourceDO extends HasIdAndAuditing {

    /**
     * 数据源的名称。
     */
    private String name;

    /**
     * 数据源的类型。
     */
    private String type;

    /**
     * 所属的组织 ID。
     */
    private String organizationId;

    /**
     * 数据源的创建来源。
     */
    private int creationSource;

    /**
     * 数据源的状态。
     */
    private DatasourceStatus datasourceStatus;

    /**
     * 数据源的详细配置。
     * 该字段使用 {@link Map} 类型存储键值对，可以存储任意的 JSON 对象。
     */
    private Map<String, Object> detailConfig;

}

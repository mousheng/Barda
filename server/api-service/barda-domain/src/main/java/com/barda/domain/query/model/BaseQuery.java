package com.barda.domain.query.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

/**
 * 该类表示基础查询。
 */
@Getter
@Builder
public class BaseQuery {

    /**
     * 数据源的标识符。
     */
    private final String datasourceId;

    /**
     * 查询的配置。
     */
    @JsonProperty(value = "comp")
    private final Map<String, Object> queryConfig;

    /**
     * 查询的类型。
     */
    private final String compType;

    /**
     * 查询的超时时间。
     */
    @JsonProperty(value = "timeout")
    private final String timeoutStr;

    /**
     * 私有构造函数，用于创建 BaseQuery 实例。
     *
     * @param datasourceId  数据源的标识符
     * @param queryConfig  查询的配置
     * @param compType  查询的类型
     * @param timeoutStr  查询的超时时间
     */
    @JsonCreator
    private BaseQuery(String datasourceId, Map<String, Object> queryConfig, String compType, String timeoutStr) {
        this.datasourceId = datasourceId;
        this.queryConfig = queryConfig;
        this.compType = compType;
        this.timeoutStr = timeoutStr;
    }
}

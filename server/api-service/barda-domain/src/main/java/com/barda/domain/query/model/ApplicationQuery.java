package com.barda.domain.query.model;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * 该类表示应用中的查询。
 */
@Getter
public class ApplicationQuery {

    /**
     * 查询的唯一标识符。
     */
    private final String id;

    /**
     * 查询的名称。
     */
    private final String name;

    /**
     * 查询的基础信息。
     */
    private final BaseQuery baseQuery;

    /**
     * 查询的触发类型。
     */
    private final String triggerType;

    /**
     * 查询的超时时间。
     */
    private final String timeoutStr;

    /**
     * 构造函数，用于创建 ApplicationQuery 实例。
     *
     * @param id  查询的唯一标识符
     * @param name  查询的名称
     * @param datasourceId  数据源的标识符
     * @param queryConfig  查询的配置
     * @param triggerType  查询的触发类型
     * @param timeoutStr  查询的超时时间
     * @param compType  查询的类型
     */
    @JsonCreator
    public ApplicationQuery(@JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("datasourceId") String datasourceId,
            @JsonProperty("comp") Map<String, Object> queryConfig,
            @JsonProperty("triggerType") String triggerType,
            @JsonProperty("timeout") String timeoutStr,
            @JsonProperty("compType") String compType) {
        this.id = id;
        this.name = name;
        this.triggerType = triggerType;
        this.timeoutStr = timeoutStr;
        this.baseQuery = BaseQuery.builder()
                .queryConfig(queryConfig)
                .datasourceId(datasourceId)
                .compType(compType)
                .timeoutStr(timeoutStr).build();
    }

    /**
     * 获取指示查询是否使用库查询的布尔值。
     *
     * @return 布尔值
     */
    public boolean isUsingLibraryQuery() {
        return "libraryQuery".equals(baseQuery.getCompType());
    }

    /**
     * 获取库查询的组合 ID。
     *
     * @return 库查询的组合 ID
     */
    public LibraryQueryCombineId getLibraryRecordQueryId() {
        String libraryQueryId = MapUtils.getString(baseQuery.getQueryConfig(), "libraryQueryId");
        String libraryQueryRecordId = MapUtils.getString(baseQuery.getQueryConfig(), "libraryQueryRecordId");
        return new LibraryQueryCombineId(libraryQueryId, libraryQueryRecordId);
    }
}

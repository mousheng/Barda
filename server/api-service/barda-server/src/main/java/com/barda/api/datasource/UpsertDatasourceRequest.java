package com.barda.api.datasource;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 该类是用于创建或更新数据源的请求。
 * 它使用了Lombok的@Getter和@Setter注解来自动生成getter和setter方法。
 */
@Getter
@Setter
public class UpsertDatasourceRequest {

    /**
     * 数据源的ID。
     */
    private String id;

    /**
     * 数据源的名称。
     */
    private String name;

    /**
     * 数据源的类型。
     */
    private String type;

    /**
     * 所属的组织ID。
     */
    private String organizationId;

    /**
     * 数据源的配置信息。
     * 它是一个Map，其中键是String类型，值是Object类型。
     */
    private Map<String, Object> datasourceConfig;
}

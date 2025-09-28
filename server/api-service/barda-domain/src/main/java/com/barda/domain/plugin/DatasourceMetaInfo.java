package com.barda.domain.plugin;

import static org.apache.commons.lang3.StringUtils.firstNonEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.barda.domain.datasource.service.DatasourceConnectionPool;

import lombok.Builder;
import lombok.Getter;

/**
 * 该类用于存储数据源的元信息。
 */
@Builder
public final class DatasourceMetaInfo {

    /**
     * 数据源类型。
     */
    private final String type;

    /**
     * 数据源的显示名称。
     */
    private final String displayName;

    /**
     * 插件执行器键。
     */
    private final String pluginExecutorKey;

    /**
     * 数据源的版本。
     */
    private final String version;

    /**
     * 指示数据源是否包含结构信息。
     */
    private final boolean hasStructureInfo;

    /**
     * 该数据源的定义。
     */
    @Getter
    private final Object definition;

    /**
     * 该数据源的连接池类型。
     */
    private final Class<? extends DatasourceConnectionPool> connectionPool;

    /**
     * 获取数据源类型。
     *
     * @return 数据源类型
     */
    @JsonProperty("id")
    public String getType() {
        return type;
    }

    /**
     * 获取数据源的显示名称。
     *
     * @return 数据源的显示名称
     */
    @JsonProperty("name")
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取插件执行器键。
     *
     * @return 插件执行器键
     */
    @JsonIgnore
    public String getPluginExecutorKey() {
        return pluginExecutorKey;
    }

    /**
     * 获取数据源的版本。如果未指定，则返回 "default"。
     *
     * @return 数据源的版本
     */
    public String getVersion() {
        return firstNonEmpty(version, "default");
    }

    /**
     * 获取指示数据源是否包含结构信息的布尔值。
     *
     * @return 布尔值
     */
    public boolean isHasStructureInfo() {
        return hasStructureInfo;
    }

    /**
     * 获取该数据源的连接池类型。
     *
     * @return 连接池类型
     */
    @JsonIgnore
    public Class<? extends DatasourceConnectionPool> getConnectionPool() {
        return connectionPool;
    }
}

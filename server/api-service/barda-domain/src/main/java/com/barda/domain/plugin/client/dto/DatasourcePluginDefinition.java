package com.barda.domain.plugin.client.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;

/**
 * 数据源插件定义类。
 * 该类用于表示数据源插件的定义，包含数据源插件的 ID、名称和数据源配置等信息。
 */
public class DatasourcePluginDefinition extends HashMap<String, Object> {

    /**
     * 获取数据源插件的 ID。
     *
     * @return 数据源插件的 ID
     */
    public String getId() {
        return MapUtils.getString(this, "id");
    }

    /**
     * 获取数据源插件的名称。
     *
     * @return 数据源插件的名称
     */
    public String getName() {
        return MapUtils.getString(this, "name");
    }

    /**
     * 判断数据源配置是否为动态类型。
     *
     * @return true：数据源配置为动态类型；false：数据源配置为非动态类型
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isDatasourceConfigExtraDynamic() {
        return Optional.ofNullable(MapUtils.getMap(this, "dataSourceConfig"))
               .map(datasourceConfig -> MapUtils.getMap((Map) datasourceConfig, "extra"))
               .map(extra -> MapUtils.getString(extra, "type", "").equals("dynamic"))
               .orElse(false);
    }

    /**
     * 判断查询配置是否为动态类型。
     *
     * @return true：查询配置为动态类型；false：查询配置为非动态类型
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isQueryConfigDynamic() {
        return Optional.ofNullable(MapUtils.getMap(this, "queryConfig"))
               .map(queryConfig -> MapUtils.getString((Map) queryConfig, "type", "").equals("dynamic"))
               .orElse(false);
    }
}

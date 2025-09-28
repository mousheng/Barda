package com.barda.plugin.clickhouse;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * ClickHouse插件类。
 * 该类是Plugin的子类，用于扩展和定制Druid的ClickHouse数据源功能。
 * 它提供对ClickHouse特定配置的访问和操作。
 */
public class ClickHousePlugin extends Plugin {

    /**
     * 构造函数。
     *
     * @param wrapper 插件包装器，提供对插件的访问和操作。
     */
    public ClickHousePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

}

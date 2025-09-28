package com.barda.plugin.snowflake;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * 用于Snowflake数据源的插件类。
 * 该类继承自Plugin，并提供对Snowflake数据源的操作和功能。
 */
public class SnowflakePlugin extends Plugin {

    /**
     * 构造函数。
     *
     * @param wrapper 插件包装器
     */
    public SnowflakePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

}

package com.barda.plugin.es;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * Elasticsearch插件。
 *
 * <p>
 * EsPlugin是Elasticsearch相关功能的入口点。
 * 它通过组合EsConnector和EsQueryExecutor来实现对Elasticsearch的操作。
 *
 * @see EsConnector
 * @see EsQueryExecutor
 */
public class EsPlugin extends Plugin {

    /**
     * 构造器。
     *
     * @param wrapper 插件包装器
     */
    public EsPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
}
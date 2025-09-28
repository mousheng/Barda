package com.barda.plugin;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * BardaApiPlugin 类是 Datasource 插件的实现。
 * 该类提供与 Barda API 进行交互的功能。
 */
public class BardaApiPlugin extends Plugin {

    /**
     * 构造函数，用于创建 BardaApiPlugin 类的实例。
     *
     * @param wrapper 插件包装器
     */
    public BardaApiPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }
}

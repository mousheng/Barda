package com.barda.plugin.restapi;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * 一个用于 REST API 插件的类。
 * 它继承自 Plugin 类，并提供了一个构造函数来初始化插件。
 */
public class RestApiPlugin extends Plugin {

    /**
     * 构造函数，用于初始化 RestApiPlugin 实例。
     *
     * @param wrapper 插件包装器，用于将插件与其包装器相关联。
     */
    public RestApiPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

}
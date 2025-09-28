package com.barda.plugin.oracle;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * OraclePlugin 类是 Plugin 的子类，
 * 用于表示 Oracle 数据库的插件。
 */
public class OraclePlugin extends Plugin {

    /**
     * 公共构造函数，
     * 用于初始化 OraclePlugin 实例。
     *
     * @param wrapper 插件包装器
     */
    public OraclePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

}

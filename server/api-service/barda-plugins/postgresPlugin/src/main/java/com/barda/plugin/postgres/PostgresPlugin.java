package com.barda.plugin.postgres;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 一个用于与PostgreSQL数据库进行交互的插件。
 *
 * 该类继承自 {@link Plugin} 并使用了Lombok的 {@link Slf4j} 注解来自动生成一个
 * 名为 {@code log} 的私有静态 final字段，该字段是 {@link Logger} 类型的，
 * 并使用了SLF4J的日志记录功能。
 */
@Slf4j
public class PostgresPlugin extends Plugin {

    public PostgresPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

}

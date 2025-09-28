package com.barda.domain.configurations;

import org.pf4j.spring.SpringPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * PF4J 配置类。
 *
 * 该类提供 PF4J 框架的配置和 bean 定义。
 */
@Configuration
public class Pf4jConfiguration {

    /**
     * 创建并返回 SpringPluginManager bean。
     *
     * SpringPluginManager 是一个 PF4J 框架的核心类，用于管理插件和扩展。
     *
     * @return 已配置的 SpringPluginManager 实例
     */
    @Bean
    public SpringPluginManager pluginManager() {
        return new SpringPluginManager();
    }
}

package com.barda.api.framework.configuration;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 该类是组件扫描配置，用于配置Spring的组件扫描功能。
 * 它使用了Spring的@ComponentScan、@Configuration和@ConditionalOnMissingClass注解来实现。
 * 该类将扫描com.barda包及其子包中的组件。
 *
 * 注意：如果存在com.barda.api.framework.configuration.ComponentScanConfigurationEEVersion类，
 * 则该配置将不会生效。
 */
@ComponentScan(basePackages = "com.barda")
@Configuration
@ConditionalOnMissingClass("com.barda.api.framework.configuration.ComponentScanConfigurationEEVersion")
public class ComponentScanConfiguration {
}

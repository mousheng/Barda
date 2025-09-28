package com.barda.api.framework.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 该类是ResourceWebPropertiesConfig的配置类，用于配置WebProperties.Resources。
 * 它使用了Spring的@Configuration注解来将该类标记为配置类。
 */
@Configuration
public class ResourceWebPropertiesConfig {

    /**
     * 该方法返回一个WebProperties.Resources的实例。
     * 它使用了Spring的@Bean注解来将该方法标记为Bean，并将其注册到Spring的IoC容器中。
     *
     * @return WebProperties.Resources的实例
     */
    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

}
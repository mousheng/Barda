package com.barda.sdk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.barda.sdk.util.JsonUtils;

/**
 * 序列化配置类，提供对 JSON 对象的序列化和反序列化的功能。
 */
@Configuration
public class SerializeConfig {

    /**
     * 创建 ObjectMapper 实例，用于 JSON 对象的序列化和反序列化。
     *
     * @return ObjectMapper 实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        return JsonUtils.getObjectMapper();
    }

    /**
     * 内部类，用于定义 JSON 视图。
     */
    public static class JsonViews {

        /**
         * 公共 JSON 视图，用于对外暴露的 API。
         */
        public static class Public {
        }

        /**
         * 内部 JSON 视图，用于内部系统之间的 API。
         */
        public static class Internal {
        }
    }
}

package com.barda.infra.config.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Builder;

/**
 * 服务器配置类，继承自 HasIdAndAuditing 类，并使用 Lombok 的 @Builder 注解来实现 Builder 模式。
 *
 * 该类使用 @Document 注解来标记为 MongoDB 文档类。
 *
 */
@Builder
@Document
public class ServerConfig extends HasIdAndAuditing {

    /**
     * 键。
     */
    private String key;

    /**
     * 值。
     *
     * 值可以是任何类型，包括原始类型、对象类型等。
     */
    private Object value;

    /**
     * 构造函数。
     *
     * 该构造函数使用 Lombok 的 @JsonCreator 注解来实现 JSON 反序列化。
     *
     * @param key 键
     * @param value 值
     */
    @JsonCreator
    public ServerConfig(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 获取键。
     *
     * @return 键
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取值。
     *
     * @return 值
     */
    public Object getValue() {
        return value;
    }
}

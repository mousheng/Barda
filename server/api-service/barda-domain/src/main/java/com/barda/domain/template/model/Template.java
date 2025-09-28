package com.barda.domain.template.model;

import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Getter;

/**
 * 模板类，表示一个模板。
 * 该类使用了 Lombok 库来自动生成 getter 方法。
 * 该类使用 Spring Data MongoDB 库来映射到 MongoDB 集合中。
 */
@Document
@Getter
public class Template extends HasIdAndAuditing {

    /**
     * 模板的名称。
     */
    private String name;

    /**
     * 该模板所引用的应用的 ID。
     */
    private String applicationId; // id of application referenced by this template
}

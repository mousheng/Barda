package com.barda.domain.application.model;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.sdk.models.HasIdAndAuditing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 该类表示应用程序状态的快照在特定时间点。
 * 它扩展了 {@link HasIdAndAuditing}，以继承通用字段，如 id、createdBy、createdDate、lastModifiedBy、
 * lastModifiedDate 等。
 *
 */
@ToString(callSuper = true)
@Document
@Getter
@Setter
public class ApplicationHistorySnapshot extends HasIdAndAuditing {

    /**
     * 应用程序的唯一标识符。
     */
    private String applicationId;

    /**
     * 应用程序的 DSL（领域特定语言）表示形式。
     * 它可以包含应用程序的配置、查询、布局等信息。
     */
    private Map<String, Object> dsl;

    /**
     * 应用程序的上下文信息。
     * 它可以包含应用程序运行时所需的环境变量、配置参数等信息。
     */
    private Map<String, Object> context;

}

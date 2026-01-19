package com.barda.domain.auditlog.model;

import java.util.Map;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.barda.infra.event.EventType;
import com.barda.sdk.models.HasIdAndAuditing;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 审计日志实体类
 * 用于记录系统中的各种操作事件
 */
@Getter
@Setter
@ToString
@Document(collection = "auditLog")
@JsonIgnoreProperties(value = {"createdBy"})
public class AuditLog extends HasIdAndAuditing {

    /**
     * 事件类型
     */
    @Indexed
    @JsonProperty(index = 3)
    private EventType eventType;

    /**
     * 目标对象（内部使用，JSON中隐藏）
     */
    @JsonIgnore
    private String target;

    /**
     * 事件详情（动态Map，根据事件类型包含不同字段）
     */
    @JsonProperty(index = 300)
    private Map<String, Object> detail;

    /**
     * 组织ID（用于数据隔离）
     */
    @Indexed
    @JsonProperty(index = 2)
    private String orgId;

    /**
     * 用户信息（查询时填充，包含id和name）
     */
    @JsonProperty(index = 200)
    private Map<String, Object> user;

    /**
     * 获取创建时间（毫秒）
     */
    public long getCreateTime() {
        return createdAt != null ? createdAt.toEpochMilli() : 0;
    }
}

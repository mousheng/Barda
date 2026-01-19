package com.barda.domain.auditlog.model;

import java.time.LocalDateTime;

import com.barda.infra.event.EventType;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

/**
 * 审计日志查询请求对象
 */
@Getter
@Setter
public class AuditLogSearchRequest {

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 事件类型
     */
    private EventType eventType;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 查询名称
     */
    private String queryName;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}

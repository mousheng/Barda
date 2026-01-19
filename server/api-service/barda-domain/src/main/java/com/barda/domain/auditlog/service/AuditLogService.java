package com.barda.domain.auditlog.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.barda.domain.auditlog.model.AuditLog;
import com.barda.domain.auditlog.model.AuditLogSearchRequest;
import com.barda.domain.auditlog.repository.AuditLogRepository;
import com.barda.infra.event.EventType;
import com.barda.infra.perf.PerfEvent;
import com.barda.infra.perf.PerfHelper;
import com.barda.sdk.util.JsonUtils;

import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 审计日志服务类
 * 负责审计日志的记录、查询和批量写入
 */
@Slf4j
@Service
public class AuditLogService {

    private static final String MONGO_AUDIT_LOG_COLLECTION = "auditLog";
    private static final String ORG_ID = "orgId";
    private static final String CREATED_AT = "createdAt";
    private static final String CREATED_BY = "createdBy";
    private static final String EVENT_TYPE = "eventType";

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    @Qualifier(value = "reactiveMongoTemplate")
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @Autowired
    private PerfHelper perfHelper;

    /**
     * 使用线程安全的队列收集日志
     */
    private volatile Queue<AuditLog> auditLogs = new ConcurrentLinkedQueue<>();

    /**
     * 记录审计日志
     *
     * @param orgId     组织ID
     * @param userId    用户ID
     * @param eventType 事件类型
     * @param detail    事件详情
     */
    public void record(String orgId, String userId, EventType eventType, Map<String, Object> detail) {
        AuditLog auditLog = new AuditLog();
        auditLog.setOrgId(orgId);
        auditLog.setCreatedBy(userId);
        auditLog.setEventType(eventType);
        auditLog.setDetail(detail);
        auditLog.setCreatedAt(Instant.now());
        record(auditLog);
    }

    /**
     * 记录审计日志到内存队列
     *
     * @param auditLog 审计日志对象
     */
    public void record(AuditLog auditLog) {
        // 验证必填字段
        if (ObjectUtils.anyNull(
                auditLog.getEventType(),
                auditLog.getOrgId(),
                auditLog.getCreatedBy()
        )) {
            log.error("fail record audit log.{}", JsonUtils.toJson(auditLog));
            return;
        }
        // 添加到队列
        this.auditLogs.add(auditLog);
    }

    /**
     * 定时批量写入（每秒执行一次）
     */
    @Scheduled(initialDelay = 1L, fixedDelay = 1L, timeUnit = TimeUnit.SECONDS)
    public void scheduledInsert() {
        if (CollectionUtils.isEmpty(this.auditLogs)) {
            return;
        }
        // 原子性交换队列
        Queue<AuditLog> tmp = this.auditLogs;
        this.auditLogs = new ConcurrentLinkedQueue<>();

        // 批量保存到数据库
        this.auditLogRepository.saveAll(tmp)
                .count()
                .subscribe(result ->
                        this.perfHelper.count(
                                PerfEvent.AUDIT_LOG_BATCH_INSERT,
                                Tags.of("size", String.valueOf(result))
                        )
                );
    }

    /**
     * 查询审计日志
     *
     * @param request 查询请求
     * @param orgId   组织ID
     * @return 审计日志列表
     */
    public Flux<AuditLog> search(AuditLogSearchRequest request, String orgId) {
        Query query = buildQuery(request, orgId);

        // 分页设置
        Pageable pageable = PageRequest.of(
                request.getPageNum() - 1,
                request.getPageSize(),
                Sort.by(CREATED_AT).descending()
        );
        query.with(pageable);

        // 查询
        return this.reactiveMongoTemplate.find(
                query,
                AuditLog.class,
                MONGO_AUDIT_LOG_COLLECTION
        );
    }

    /**
     * 统计审计日志数量
     *
     * @param request 查询请求
     * @param orgId   组织ID
     * @return 日志数量
     */
    public Mono<Long> count(AuditLogSearchRequest request, String orgId) {
        Query query = buildQuery(request, orgId);
        return this.reactiveMongoTemplate.count(
                query,
                AuditLog.class,
                MONGO_AUDIT_LOG_COLLECTION
        );
    }

    /**
     * 构建查询条件
     *
     * @param request 查询请求
     * @param orgId   组织ID
     * @return 查询对象
     */
    private Query buildQuery(AuditLogSearchRequest request, String orgId) {
        Query query = new Query();

        // 1. 强制组织隔离（必须）
        query.addCriteria(Criteria.where(ORG_ID).is(orgId));

        // 2. 时间范围筛选
        if (ObjectUtils.anyNotNull(request.getStartTime(), request.getEndTime())) {
            Criteria criteria = Criteria.where(CREATED_AT);
            if (request.getStartTime() != null) {
                Instant startInstant = request.getStartTime()
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
                criteria.gte(startInstant);
            }
            if (request.getEndTime() != null) {
                Instant endInstant = request.getEndTime()
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
                criteria.lte(endInstant);
            }
            query.addCriteria(criteria);
        }

        // 3. 用户筛选
        if (StringUtils.isNotBlank(request.getUserId())) {
            query.addCriteria(Criteria.where(CREATED_BY).is(request.getUserId()));
        }

        // 4. 事件类型筛选
        if (Objects.nonNull(request.getEventType())) {
            query.addCriteria(Criteria.where(EVENT_TYPE).is(request.getEventType()));
        }

        // 5. 应用ID筛选（通过detail字段）
        if (StringUtils.isNotBlank(request.getAppId())) {
            query.addCriteria(Criteria.where("detail.applicationId").is(request.getAppId()));
        }

        return query;
    }
}

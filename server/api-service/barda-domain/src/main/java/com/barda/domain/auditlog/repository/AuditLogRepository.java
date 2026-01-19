package com.barda.domain.auditlog.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.barda.domain.auditlog.model.AuditLog;

/**
 * 审计日志仓库接口
 * 提供对审计日志的数据库访问操作
 */
@Repository
public interface AuditLogRepository extends ReactiveMongoRepository<AuditLog, String> {
}

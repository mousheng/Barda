package com.barda.domain.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;

import com.barda.domain.application.model.ApplicationHistorySnapshot;

import reactor.core.publisher.Mono;

/**
 * 应用历史快照服务接口，提供应用历史快照相关的操作。
 */
public interface ApplicationHistorySnapshotService {

    /**
     * 创建应用历史快照。
     *
     * @param applicationId 应用ID
     * @param dsl 应用DSL
     * @param context 上下文
     * @param userId 创建者ID
     * @return 创建是否成功的Mono对象
     */
    Mono<Boolean> createHistorySnapshot(String applicationId, Map<String, Object> dsl, Map<String, Object> context, String userId);

    /**
     * 获取应用的所有历史快照的简要信息。
     *
     * @param applicationId 应用ID
     * @param pageRequest 分页请求
     * @return 应用历史快照列表Mono对象
     */
    Mono<List<ApplicationHistorySnapshot>> listAllHistorySnapshotBriefInfo(String applicationId, PageRequest pageRequest);

    /**
     * 统计指定应用ID的应用历史快照数量。
     *
     * @param applicationId 应用ID
     * @return 应用历史快照数量Mono对象
     */
    Mono<Long> countByApplicationId(String applicationId);

    /**
     * 获取应用历史快照的详细信息。
     *
     * @param historySnapshotId 应用历史快照ID
     * @return 应用历史快照Mono对象
     */
    Mono<ApplicationHistorySnapshot> getHistorySnapshotDetail(String historySnapshotId);
}

package com.barda.domain.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.barda.domain.user.constant.UserStatusType;
import com.barda.domain.user.model.UserStatus;
import com.barda.domain.user.repository.UserStatusRepository;
import com.barda.infra.mongo.MongoUpsertHelper;

import reactor.core.publisher.Mono;

/**
 * 用户状态管理的服务类。
 *
 */
@Lazy
@Service
public class UserStatusService {

    /**
     * 用户状态的 MongoDB 存储库。
     */
    @Autowired
    private UserStatusRepository repository;

    /**
     * 用于执行 MongoDB upsert 操作的帮助类。
     */
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 根据用户 ID 查询用户状态。
     * 如果找不到，返回一个新的 UserStatus 对象并将其 ID 设置为 userId。
     *
     * @param userId 用户 ID
     * @return 包含用户状态的 Mono 对象
     */
    public Mono<UserStatus> findByUserId(String userId) {
        return repository.findById(userId)
                .defaultIfEmpty(UserStatus.builder()
                        .id(userId)
                        .build()
                );
    }

    /**
     * 标记新用户引导已显示。
     *
     * @param userId 用户 ID
     * @return 标记操作是否成功的 Mono 对象
     */
    public Mono<Boolean> markNewUserGuidanceShown(String userId) {
        UserStatus userStatus = UserStatus.builder()
                .id(userId)
                .hasShowNewUserGuidance(true)
                .build();
        return mongoUpsertHelper.upsert(userStatus, "id", userId);
    }

    /**
     * 标记用户状态。
     *
     * @param userId 用户 ID
     * @param statusType 要标记的状态类型
     * @param value 要标记的值
     * @return 标记操作是否成功的 Mono 对象
     */
    public Mono<Boolean> mark(String userId, UserStatusType statusType, Object value) {
        Update update = Update.update("statusMap." + statusType.getValue(), value);
        return mongoUpsertHelper.upsert(update, "id", userId, UserStatus.class);
    }
}

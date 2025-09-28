package com.barda.domain.group.service;

import java.util.Collection;

import com.barda.domain.group.model.Group;
import com.barda.infra.mongo.MongoUpsertHelper.PartialResourceWithId;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 群组管理的服务接口。
 */
public interface GroupService {

    /**
     * 获取指定 ID 的群组。
     *
     * @param groupId 群组 ID
     * @return 群组 Mono
     */
    Mono<Group> getById(String groupId);

    /**
     * 获取指定 ID 列表的群组。
     *
     * @param groupIds 群组 ID 列表
     * @return 群组 Flux
     */
    Flux<Group> getByIds(Collection<String> groupIds);

    /**
     * 获取指定组织 ID 的所有群组。
     *
     * @param organizationId 组织 ID
     * @return 群组 Flux
     */
    Flux<Group> getByOrgId(String organizationId);

    /**
     * 获取指定组织 ID 的群组数量。
     *
     * @param organizationId 组织 ID
     * @return 群组数量 Mono
     */
    default Mono<Long> getOrgGroupCount(String organizationId) {
        return getByOrgId(organizationId)
                .count();
    }

    /**
     * 删除指定 ID 的群组。
     *
     * @param id 群组 ID
     * @return 空 Mono
     */
    Mono<Void> delete(String id);

    /**
     * 创建新的群组。
     *
     * @param newGroup 新群组
     * @param userId 创建者用户 ID
     * @param orgId 所属组织 ID
     * @return 新群组 Mono
     */
    Mono<Group> create(Group newGroup, String userId, String orgId);

    /**
     * 更新群组。
     *
     * @param updateGroup 要更新的群组
     * @return 成功返回 true，否则返回 false
     */
    Mono<Boolean> updateGroup(Group updateGroup);

    /**
     * 获取指定组织 ID 的所有用户群组。
     *
     * @param organizationId 组织 ID
     * @return 所有用户群组 Mono
     */
    Mono<Group> getAllUsersGroup(String organizationId);

    /**
     * 获取指定组织 ID 的开发者群组。
     *
     * @param orgId 组织 ID
     * @return 开发者群组 Mono
     */
    Mono<Group> getDevGroup(String orgId);

    /**
     * 创建指定组织 ID 的开发者群组。
     *
     * @param orgId 组织 ID
     * @return 新创建的开发者群组 Mono
     */
    Mono<Group> createDevGroup(String orgId);

    /**
     * 创建指定组织 ID 的所有用户群组。
     *
     * @param orgId 组织 ID
     * @return 新创建的所有用户群组 Mono
     */
    Mono<Group> createAllUserGroup(String orgId);

    /**
     * 获取指定组织 ID 和来源的群组列表。
     *
     * @param orgId 组织 ID
     * @param source 来源
     * @return 群组 Flux
     */
    Flux<Group> getAllGroupsBySource(String orgId, String source);

    /**
     * 批量创建并同步群组。
     *
     * @param groups 群组列表
     * @return 成功返回 true，否则返回 false
     */
    Mono<Boolean> bulkCreateSyncGroup(Collection<Group> groups);

    /**
     * 批量更新群组。
     *
     * @param groups 要更新的群组列表
     * @return 成功返回 true，否则返回 false
     */
    Mono<Boolean> bulkUpdateGroup(Collection<PartialResourceWithId<Group>> groups);
}

package com.barda.domain.group.service;

import static com.barda.domain.group.util.SystemGroups.ALL_USER;
import static com.barda.domain.group.util.SystemGroups.DEV;
import static com.barda.sdk.util.LocaleUtils.getLocale;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.barda.domain.group.event.GroupDeletedEvent;
import com.barda.domain.group.model.Group;
import com.barda.domain.group.repository.GroupRepository;
import com.barda.domain.group.util.SystemGroups;
import com.barda.domain.organization.model.MemberRole;
import com.barda.infra.mongo.MongoUpsertHelper;
import com.barda.infra.mongo.MongoUpsertHelper.PartialResourceWithId;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 实现 GroupService 接口，提供群组管理功能的服务类。
 */
@Service
@Slf4j
public class GroupServiceImpl implements GroupService {

    /**
     * 群组仓库。
     */
    @Autowired
    private GroupRepository repository;

    /**
     * 群组成员管理的服务。
     */
    @Autowired
    private GroupMemberService groupMemberService;

    /**
     * MongoDB upsert 帮助类。
     */
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 应用上下文。
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 根据群组ID获取群组信息。
     *
     * @param groupId 群组ID
     * @return 群组信息的 Mono 对象
     */
    @Override
    public Mono<Group> getById(String groupId) {
        return repository.findById(groupId);
    }

    /**
     * 根据多个群组ID获取群组信息。
     *
     * @param groupIds 群组ID集合
     * @return 群组信息的 Flux 对象
     */
    @Override
    public Flux<Group> getByIds(Collection<String> groupIds) {
        return repository.findByIdIn(groupIds);
    }

    /**
     * 根据组织ID获取该组织下的所有群组信息。
     *
     * @param organizationId 组织ID
     * @return 该组织下所有群组信息的 Flux 对象
     */
    @Override
    public Flux<Group> getByOrgId(String organizationId) {
        return this.repository.findByOrganizationId(organizationId);
    }

    /**
     * 根据组织ID获取该组织下的群组数量。
     *
     * @param organizationId 组织ID
     * @return 该组织下群组数量的 Mono 对象
     */
    @Override
    public Mono<Long> getOrgGroupCount(String organizationId) {
        return repository.countByOrganizationId(organizationId);
    }

    /**
     * 根据群组ID删除群组。
     *
     * @param id 群组ID
     * @return 删除操作的 Mono 对象
     */
    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id)
                .then(Mono.defer(() -> sendGroupDeletedEvent(id)));
    }

    /**
     * 发送群组删除事件。
     *
     * @param groupId 群组ID
     * @return 事件发送操作的 Mono 对象
     */
    private Mono<Void> sendGroupDeletedEvent(String groupId) {
        GroupDeletedEvent event = new GroupDeletedEvent();
        event.setGroupId(groupId);
        applicationContext.publishEvent(event);
        return Mono.empty();
    }

    /**
     * 创建新的群组。
     *
     * @param newGroup 新群组对象
     * @param userId   用户ID
     * @param orgId    组织ID
     * @return 创建后的群组信息的 Mono 对象
     */
    @Override
    public Mono<Group> create(Group newGroup, String userId, String orgId) {
        return repository.save(newGroup)
                .flatMap(createdGroup -> groupMemberService.addMember(orgId, createdGroup.getId(),
                        userId, MemberRole.ADMIN).thenReturn(createdGroup));
    }

    /**
     * 更新群组信息。
     *
     * @param updateGroup 更新后的群组对象
     * @return 是否更新成功的 Mono 对象
     */
    @Override
    public Mono<Boolean> updateGroup(Group updateGroup) {
        return mongoUpsertHelper.updateById(updateGroup, updateGroup.getId());
    }

    /**
     * 获取组织中的所有用户群组。
     *
     * @param organizationId 组织ID
     * @return 所有用户群组的 Mono 对象
     */
    @Override
    public Mono<Group> getAllUsersGroup(String organizationId) {
        return repository.findByOrganizationIdAndAllUsersGroup(organizationId, true);
    }

    /**
     * 获取组织中的开发群组。
     *
     * @param orgId 组织ID
     * @return 开发群组的 Mono 对象
     */
    @Override
    public Mono<Group> getDevGroup(String orgId) {
        return repository.findByOrganizationIdAndType(orgId, DEV);
    }

    /**
     * 创建系统群组。
     *
     * @param organizationId 组织ID
     * @param type           群组类型
     * @return 创建后的群组信息的 Mono 对象
     */
    private Mono<Group> createSystemGroup(String organizationId, String type) {
        return Mono.deferContextual(contextView -> {
            Locale locale = getLocale(contextView);
            Group group = new Group();
            group.setOrganizationId(organizationId);
            group.setName(SystemGroups.getName(type, locale));
            group.setType(type);
            group.setAllUsersGroup(type.equals(ALL_USER));
            return repository.save(group);
        });
    }

    /**
     * 创建开发群组。
     *
     * @param orgId 组织ID
     * @return 创建后的开发群组的 Mono 对象
     */
    @Override
    public Mono<Group> createDevGroup(String orgId) {
        return createSystemGroup(orgId, DEV);
    }

    /**
     * 创建所有用户群组。
     *
     * @param orgId 组织ID
     * @return 创建后的所有用户群组的 Mono 对象
     */
    @Override
    public Mono<Group> createAllUserGroup(String orgId) {
        return createSystemGroup(orgId, ALL_USER);
    }

    /**
     * 批量同步创建群组。
     *
     * @param groups 群组集合
     * @return 是否创建成功的 Mono 对象
     */
    @Override
    public Mono<Boolean> bulkCreateSyncGroup(Collection<Group> groups) {
        return repository.saveAll(groups).hasElements();
    }

    /**
     * 获取特定来源的所有群组。
     *
     * @param orgId  组织ID
     * @param source 群组来源
     * @return 特定来源的所有群组的 Flux 对象
     */
    @Override
    public Flux<Group> getAllGroupsBySource(String orgId, String source) {
        return repository.findBySourceAndOrganizationId(source, orgId);
    }

    /**
     * 批量更新群组。
     *
     * @param groups 群组部分资源集合
     * @return 是否更新成功的 Mono 对象
     */
    @Override
    public Mono<Boolean> bulkUpdateGroup(Collection<PartialResourceWithId<Group>> groups) {
        return mongoUpsertHelper.bulkUpdate(groups);
    }
}

package com.barda.domain.group.service;

import static com.barda.infra.birelation.BiRelationBizType.GROUP_MEMBER;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.group.model.Group;
import com.barda.domain.group.model.GroupMember;
import com.barda.domain.organization.model.MemberRole;
import com.barda.domain.organization.model.OrgMemberState;
import com.barda.infra.birelation.BiRelation;
import com.barda.infra.birelation.BiRelationService;
import com.barda.infra.mongo.MongoUpsertHelper;

import reactor.core.publisher.Mono;

/**
 * 群组成员管理的服务实现类。
 */
@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    /**
     * 双向关系服务。
     */
    @Autowired
    private BiRelationService biRelationService;

    /**
     * MongoDB upsert 帮助类。
     */
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 获取群组成员。
     *
     * @param groupId 群组ID
     * @param page    页码
     * @param count   每页数量
     * @return 群组成员列表的 Mono 对象
     */
    @Override
    public Mono<List<GroupMember>> getGroupMembers(String groupId, int page, int count) {
        return biRelationService.getBySourceId(GROUP_MEMBER, groupId)
                .map(GroupMember::from)
                .collectList();
    }

    /**
     * 添加成员到群组。
     *
     * @param orgId      组织ID
     * @param groupId    群组ID
     * @param userId     用户ID
     * @param memberRole 成员角色
     * @return 是否添加成功的 Mono 对象
     */
    @Override
    public Mono<Boolean> addMember(String orgId, String groupId, String userId, MemberRole memberRole) {
        return biRelationService.addBiRelation(GROUP_MEMBER, groupId,
                        userId, memberRole.getValue(), OrgMemberState.NORMAL.getValue(), orgId)
                .hasElement();
    }

    /**
     * 更新群组成员角色。
     *
     * @param groupId    群组ID
     * @param userId     用户ID
     * @param memberRole 成员角色
     * @return 是否更新成功的 Mono 对象
     */
    @Override
    public Mono<Boolean> updateMemberRole(String groupId, String userId, MemberRole memberRole) {
        return biRelationService.updateRelation(GROUP_MEMBER, groupId, userId, memberRole.getValue())
                .hasElement();
    }

    /**
     * 从群组中移除成员。
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 是否移除成功的 Mono 对象
     */
    @Override
    public Mono<Boolean> removeMember(String groupId, String userId) {
        return biRelationService.removeBiRelation(GROUP_MEMBER, groupId, userId);
    }

    /**
     * 获取用户在组织中的群组ID列表。
     *
     * @param orgId  组织ID
     * @param userId 用户ID
     * @return 群组ID列表的 Mono 对象
     */
    @Override
    public Mono<List<String>> getUserGroupIdsInOrg(String orgId, String userId) {
        return getNonDynamicUserGroupIdsInOrg(orgId, userId);
    }

    /**
     * 获取用户在组织中的群组成员信息。
     *
     * @param orgId  组织ID
     * @param userId 用户ID
     * @return 群组成员列表的 Mono 对象
     */
    @Override
    public Mono<List<GroupMember>> getUserGroupMembersInOrg(String orgId, String userId) {
        return biRelationService.getByTargetId(GROUP_MEMBER, userId)
                .map(GroupMember::from)
                .filter(it -> StringUtils.equals(it.getOrgId(), orgId))
                .collectList();
    }

    /**
     * 获取群组中特定成员的信息。
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return 群组成员的 Mono 对象
     */
    @Override
    public Mono<GroupMember> getGroupMember(String groupId, String userId) {
        return biRelationService.getBiRelation(GROUP_MEMBER, groupId, userId)
                .map(GroupMember::from);
    }

    /**
     * 获取群组中的所有管理员。
     *
     * @param groupId 群组ID
     * @return 群组管理员列表的 Mono 对象
     */
    @Override
    public Mono<List<GroupMember>> getAllGroupAdmin(String groupId) {
        return biRelationService.getBySourceIdAndRelation(GROUP_MEMBER, groupId, MemberRole.ADMIN.getValue())
                .map(GroupMember::from)
                .collectList();
    }

    /**
     * 删除群组中的所有成员。
     *
     * @param groupId 群组ID
     * @return 是否删除成功的 Mono 对象
     */
    @Override
    public Mono<Boolean> deleteGroupMembers(String groupId) {
        return biRelationService.removeAllBiRelations(GROUP_MEMBER, groupId);
    }

    /**
     * 检查用户是否为群组成员。
     *
     * @param group  群组对象
     * @param userId 用户ID
     * @return 是否为群组成员的 Mono 对象
     */
    @Override
    public Mono<Boolean> isMember(Group group, String userId) {
        return biRelationService.getBiRelation(GROUP_MEMBER, group.getId(), userId)
                .hasElement();
    }

    /**
     * 获取用户在组织中非动态群组的群组ID列表。
     *
     * @param orgId  组织ID
     * @param userId 用户ID
     * @return 非动态群组ID列表的 Mono 对象
     */
    @Override
    public Mono<List<String>> getNonDynamicUserGroupIdsInOrg(String orgId, String userId) {
        return biRelationService.getByTargetId(GROUP_MEMBER, userId)
                .map(GroupMember::from)
                .filter(it -> StringUtils.equals(it.getOrgId(), orgId))
                .map(GroupMember::getGroupId)
                .collectList();
    }

    /**
     * 批量添加成员到群组。
     *
     * @param groupMembers 群组成员集合
     * @return 添加成功的群组成员列表的 Mono 对象
     */
    @Override
    public Mono<List<GroupMember>> bulkAddMember(Collection<GroupMember> groupMembers) {
        List<BiRelation> biRelations = groupMembers.stream()
                .map(groupMember -> BiRelation.builder()
                        .bizType(GROUP_MEMBER)
                        .sourceId(groupMember.getGroupId())
                        .targetId(groupMember.getUserId())
                        .relation(MemberRole.MEMBER.getValue())
                        .state(OrgMemberState.NORMAL.getValue())
                        .extParam1(groupMember.getOrgId())
                        .build())
                .toList();
        return biRelationService.batchAddBiRelation(biRelations)
                .map(r -> r.stream().map(GroupMember::from).toList());
    }

    /**
     * 批量移除群组成员。
     *
     * @param groupId 群组ID
     * @param userIds 用户ID集合
     * @return 是否移除成功的 Mono 对象
     */
    @Override
    public Mono<Boolean> bulkRemoveMember(String groupId, Collection<String> userIds) {
        List<Document> filters = userIds.stream()
                .map(userId -> new Document(Map.of("bizType", GROUP_MEMBER.name(), "sourceId", groupId, "targetId", userId)))
                .toList();
        return mongoUpsertHelper.bulkRemove(filters, BiRelation.class);
    }
}

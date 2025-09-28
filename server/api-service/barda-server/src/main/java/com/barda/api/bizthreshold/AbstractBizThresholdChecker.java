package com.barda.api.bizthreshold;

import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.service.ApplicationService;
import com.barda.domain.group.model.GroupMember;
import com.barda.domain.group.service.GroupMemberService;
import com.barda.domain.group.service.GroupService;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.infra.util.TupleUtils;
import com.barda.sdk.exception.BizError;

import reactor.core.publisher.Mono;

/**
 * 抽象的业务阈值检查器类，提供检查组织、组、应用等的最大数量的功能。
 */
@Component
public abstract class AbstractBizThresholdChecker {

    /**
     * 组织成员服务。
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 组服务。
     */
    @Autowired
    private GroupService groupService;

    /**
     * 组成员服务。
     */
    @Autowired
    private GroupMemberService groupMemberService;

    /**
     * 应用服务。
     */
    @Autowired
    private ApplicationService applicationService;

    /**
     * 获取单个用户可以拥有的最大组织数量。
     *
     * @return 单个用户可以拥有的最大组织数量。
     */
    protected abstract int getMaxOrgPerUser();

    /**
     * 获取单个组织可以拥有的最大成员数量。
     *
     * @return 单个组织可以拥有的最大成员数量。
     */
    protected abstract int getMaxOrgMemberCount();

    /**
     * 获取单个组织可以拥有的最大组数量。
     *
     * @return 单个组织可以拥有的最大组数量。
     */
    protected abstract int getMaxOrgGroupCount();

    /**
     * 获取单个组织可以拥有的最大应用数量。
     *
     * @return 单个组织可以拥有的最大应用数量。
     */
    protected abstract int getMaxOrgAppCount();

    /**
     * 获取用户组织数量的白名单。
     *
     * @return 用户组织数量的白名单。
     */
    protected abstract Map<String, Integer> getUserOrgCountWhiteList();

    /**
     * 获取组织成员数量的白名单。
     *
     * @return 组织成员数量的白名单。
     */
    protected abstract Map<String, Integer> getOrgMemberCountWhiteList();

    /**
     * 获取组织应用数量的白名单。
     *
     * @return 组织应用数量的白名单。
     */
    protected abstract Map<String, Integer> getOrgAppCountWhiteList();

    /**
     * 获取最大的开发者数量。
     *
     * @return 最大的开发者数量。
     */
    protected abstract Mono<Integer> getMaxDeveloperCount();

    /**
     * 检查单个用户可以拥有的最大组织数量。
     *
     * @param userId 用户ID。
     * @return 一个Mono，表示操作完成。
     */
    public Mono<Void> checkMaxOrgCount(String userId) {
        return orgMemberService.countAllActiveOrgs(userId)
                .filter(userOrgCount -> userOrgCountBelowThreshold(userId, userOrgCount))
                .switchIfEmpty(deferredError(BizError.EXCEED_MAX_USER_ORG_COUNT, "EXCEED_MAX_USER_ORG_COUNT"))
                .then();
    }

    /**
     * 检查用户组织数量是否低于阈值。
     *
     * @param userId       用户ID
     * @param userOrgCount 用户的组织数量
     * @return 如果用户的组织数量低于阈值，则返回 true；否则返回 false
     */
    private boolean userOrgCountBelowThreshold(String userId, long userOrgCount) {
        return userOrgCount < Math.max(getUserOrgCountWhiteList().getOrDefault(userId, 0),
                getMaxOrgPerUser());
    }

    /**
     * 检查组织成员数量是否超过阈值。
     *
     * @param orgId 组织ID
     * @return 如果组织成员数量未超过阈值，则返回一个空的Mono；否则返回一个包含错误信息的Mono
     */
    public Mono<Void> checkMaxOrgMemberCount(String orgId) {
        return orgMemberService.getOrgMemberCount(orgId)
                .filter(orgMemberCount -> orgMemberCountBelowThreshold(orgId, orgMemberCount))
                .switchIfEmpty(deferredError(BizError.EXCEED_MAX_ORG_MEMBER_COUNT, "EXCEED_MAX_ORG_MEMBER_COUNT"))
                .then();
    }

    /**
     * 检查组织成员数量是否低于阈值。
     *
     * @param orgId         组织ID
     * @param orgMemberCount 组织成员数量
     * @return 如果组织成员数量低于阈值，则返回true；否则返回false
     */
    private boolean orgMemberCountBelowThreshold(String orgId, Long orgMemberCount) {
        return orgMemberCount < Math.max(getMaxOrgMemberCount(), getOrgMemberCountWhiteList().getOrDefault(orgId, 0));
    }

    /**
     * 检查组织下的群组数量是否超过阈值。
     *
     * @param orgMemberMono 包含组织成员信息的Mono
     * @return 如果群组数量未超过阈值，则返回一个空的Mono；否则返回一个包含错误信息的Mono
     */
    public Mono<Void> checkMaxGroupCount(OrgMember orgMemberMono) {
        return groupService.getOrgGroupCount(orgMemberMono.getOrgId())
                .filter(it -> it < getMaxOrgGroupCount())
                .switchIfEmpty(deferredError(BizError.EXCEED_MAX_GROUP_COUNT, "EXCEED_MAX_GROUP_COUNT"))
                .then();
    }

    /**
     * 检查组织下的应用程序数量是否超过阈值。
     *
     * @param orgMember 包含组织成员信息的对象
     * @return 如果应用程序数量未超过阈值，则返回一个空的Mono；否则返回一个包含错误信息的Mono
     */
    public Mono<Void> checkMaxOrgApplicationCount(OrgMember orgMember) {
        String orgId = orgMember.getOrgId();
        return applicationService.countByOrganizationId(orgId, ApplicationStatus.NORMAL)
                .filter(orgAppCount -> orgAppCountBelowThreshold(orgId, orgAppCount))
                .switchIfEmpty(deferredError(BizError.EXCEED_MAX_APP_COUNT, "EXCEED_MAX_APP_COUNT"))
                .then();
    }

    /**
     * 检查组织的应用程序数量是否低于阈值。
     *
     * @param orgId      组织ID
     * @param orgAppCount 组织的应用程序数量
     * @return 如果组织的应用程序数量低于阈值，则返回true；否则返回false
     */
    private boolean orgAppCountBelowThreshold(String orgId, long orgAppCount) {
        return orgAppCount < Math.max(getMaxOrgAppCount(), getOrgAppCountWhiteList().getOrDefault(orgId, 0));
    }

    /**
     * 检查组织下的开发者数量是否超过阈值。
     *
     * @param orgId           组织ID
     * @param developGroupId  开发组ID
     * @param userId          用户ID
     * @return 如果开发者数量未超过阈值，则返回一个空的Mono；否则返回一个包含错误信息的Mono
     */
    public Mono<Void> checkMaxDeveloperCount(String orgId, String developGroupId, String userId) {
        return orgMemberService.getAllOrgAdmins(orgId)
                .zipWith(groupMemberService.getGroupMembers(developGroupId, 1, 100))
                .zipWith(getMaxDeveloperCount(), TupleUtils::merge)
                .flatMap(tuple -> {
                    List<OrgMember> t1 = tuple.getT1();
                    List<GroupMember> t2 = tuple.getT2();
                    Integer t3 = tuple.getT3();
                    Set<String> developerIds = Stream.concat(t1.stream().map(OrgMember::getUserId), t2.stream().map(GroupMember::getUserId))
                            .collect(Collectors.toSet());
                    developerIds.add(userId);
                    if (developerIds.size() > t3) {
                        return ofError(BizError.EXCEED_MAX_DEVELOPER_COUNT, "EXCEED_MAX_DEVELOPER_COUNT");
                    }
                    return Mono.empty();
                });
    }
}

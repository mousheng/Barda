package com.barda.api.usermanagement;

import com.barda.api.authentication.dto.OrganizationDomainCheckResult;
import com.barda.api.bizthreshold.AbstractBizThresholdChecker;
import com.barda.api.config.ConfigView;
import com.barda.api.home.SessionUserService;
import com.barda.api.usermanagement.view.OrgMemberListView;
import com.barda.api.usermanagement.view.OrgMemberListView.OrgMemberView;
import com.barda.api.usermanagement.view.OrgView;
import com.barda.api.usermanagement.view.UpdateOrgRequest;
import com.barda.api.usermanagement.view.UpdateRoleRequest;
import com.barda.domain.authentication.AuthenticationService;
import com.barda.domain.authentication.FindAuthConfig;
import com.barda.domain.group.service.GroupService;
import com.barda.domain.organization.event.OrgMemberLeftEvent;
import com.barda.domain.organization.model.MemberRole;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.model.Organization.OrganizationCommonSettings;
import com.barda.domain.organization.model.OrganizationDomain;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.organization.service.OrganizationService;
import com.barda.domain.user.model.Connection;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.auth.AbstractAuthConfig;
import com.barda.sdk.auth.EmailAuthConfig;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.config.CommonConfig.Workspace;
import com.barda.sdk.constants.WorkspaceMode;
import com.barda.sdk.encryption.RSACryptoServiceImpl;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.util.UriUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

import static com.barda.sdk.exception.BizError.LAST_ADMIN_CANNOT_LEAVE_ORG;
import static com.barda.sdk.exception.BizError.UNSUPPORTED_OPERATION;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static com.barda.sdk.util.StreamUtils.collectSet;

/**
 * 组织相关的API实现类。
 */
@Slf4j
@Service
public class OrgApiServiceImpl implements OrgApiService {

    /**
     * 用于获取当前会话用户的服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 组织成员相关的服务。
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 用户相关的服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 组织相关的服务。
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * 业务阈值检查器。
     */
    @Autowired
    private AbstractBizThresholdChecker bizThresholdChecker;

    /**
     * 应用上下文。
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 通用配置。
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 组相关的服务。
     */
    @Autowired
    private GroupService groupService;

    /**
     * 认证相关的服务。
     */
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private RSACryptoServiceImpl rsacryptoService;

    /**
     * 获取组织的成员列表。
     *
     * @param orgId 组织ID
     * @param page  页码
     * @param count 数量
     * @return 组织成员列表
     */
    @Override
    public Mono<OrgMemberListView> getOrganizationMembers(String orgId, int page, int count) {
        return sessionUserService.getVisitorId()
                .flatMap(visitorId -> orgMemberService.getOrgMember(orgId, visitorId))
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, "NOT_AUTHORIZED"))
                .then(getOrgMemberListView(orgId, page, count));
    }

    /**
     * 获取组织成员列表的视图。
     *
     * @param orgId 组织ID
     * @param page  页码
     * @param count 数量
     * @return 组织成员列表的视图
     */
    private Mono<OrgMemberListView> getOrgMemberListView(String orgId, int page, int count) {
        return orgMemberService.getOrganizationMembers(orgId, page, count)
                .collectList()
                .flatMap(orgMembers -> {
                    List<String> userIds = orgMembers.stream()
                            .map(OrgMember::getUserId)
                            .collect(Collectors.toList());
                    Mono<Map<String, User>> users = userService.getByIds(userIds);

                    return users.map(map -> orgMembers.stream()
                            .map(orgMember -> {
                                User user = map.get(orgMember.getUserId());
                                if (user == null) {
                                    log.warn("用户 {} 不存在，将从结果中移除。", orgMember.getUserId());
                                    return null;
                                }
                                return build(user, orgMember);
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
                    );
                })
                .zipWith(sessionUserService.getVisitorOrgMemberCache())
                .map(tuple -> {
                    List<OrgMemberView> orgMemberViews = tuple.getT1();
                    OrgMember orgMember = tuple.getT2();
                    return OrgMemberListView.builder()
                            .members(orgMemberViews)
                            .visitorRole(orgMember.getRole().getValue())
                            .build();
                });
    }

    /**
     * 构建组织成员视图。
     *
     * @param user      用户
     * @param orgMember 组织成员
     * @return 组织成员视图
     */
    protected OrgMemberView build(User user, OrgMember orgMember) {
        String orgId = orgMember.getOrgId();
        return OrgMemberView.builder()
                .name(user.getName())
                .userId(user.getId())
                .role(orgMember.getRole().getValue())
                .avatarUrl(user.getAvatarUrl())
                .joinTime(orgMember.getJoinTime())
                .rawUserInfos(findRawUserInfos(user, orgId))
                .build();
    }

    /**
     * 获取用户的原始用户信息。
     *
     * @param user  用户
     * @param orgId 组织ID
     * @return 原始用户信息
     */
    protected Map<String, Map<String, Object>> findRawUserInfos(User user, String orgId) {
        return SetUtils.emptyIfNull(user.getConnections())
                .stream()
                .filter(connection -> {
                    if (commonConfig.isCloud()) {
                        return connection.containOrg(orgId);
                    }
                    return true;
                })
                .collect(Collectors.toMap(Connection::getSource, Connection::getRawUserInfo, (map, map2) -> map));
    }

    /**
     * 更新组织成员的角色。
     *
     * @param orgId             组织ID
     * @param updateRoleRequest 更新角色请求
     * @return 是否更新成功
     */
    @Override
    public Mono<Boolean> updateRoleForMember(String orgId, UpdateRoleRequest updateRoleRequest) {
        return checkVisitorAdminRole(orgId)
                .then(checkDeveloperCount(orgId, updateRoleRequest.getRole(), updateRoleRequest.getUserId()))
                .then(orgMemberService.updateMemberRole(orgId,
                        updateRoleRequest.getUserId(),
                        MemberRole.fromValue(updateRoleRequest.getRole())));
    }

    /**
     * 检查访客是否是管理员。
     *
     * @param orgId 组织ID
     * @return 访客是否是管理员
     */
    private Mono<OrgMember> checkVisitorAdminRole(String orgId) {
        return sessionUserService.getVisitorId()
                .flatMap(visitor -> orgMemberService.getOrgMember(orgId, visitor))
                .filter(it -> it.getRole() == MemberRole.ADMIN)
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, "NOT_AUTHORIZED"));
    }

    /**
     * 检查开发者人数是否超出阈值。
     *
     * @param orgId  组织ID
     * @param role   角色
     * @param userId 用户ID
     * @return 是否超出阈值
     */
    private Mono<Void> checkDeveloperCount(String orgId, String role, String userId) {
        if (!MemberRole.isAdmin(role)) {
            return Mono.empty();
        }
        return groupService.getDevGroup(orgId)
                .flatMap(group -> bizThresholdChecker.checkMaxDeveloperCount(orgId, group.getId(), userId));
    }


    /**
     * 切换当前组织到指定组织。
     *
     * @param nextCurrentOrgId 下一个当前组织的ID。
     * @return 如果成功切换当前组织，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> switchCurrentOrganizationTo(String nextCurrentOrgId) {
        return sessionUserService.getVisitorId()
                .flatMap(it -> orgMemberService.getAllActiveOrgs(it).collectList())
                .defaultIfEmpty(Collections.emptyList())
                .flatMap(orgMembers -> {
                    // 检查下一个当前组织ID是否在用户所有的组织中
                    if (!collectSet(orgMembers, OrgMember::getOrgId).contains(nextCurrentOrgId)) {
                        return Mono.error(new BizException(BizError.INVALID_ORG_ID, "INVALID_ORG_ID"));
                    }

                    String userId = orgMembers.get(0).getUserId();

                    // 查找之前的当前组织成员
                    Optional<OrgMember> previousCurrentOrgMember = orgMembers.stream()
                            .filter(OrgMember::isCurrentOrg)
                            .findFirst();
                    if (previousCurrentOrgMember.isPresent()) {
                        OrgMember orgMember = previousCurrentOrgMember.get();
                        String previousCurrentOrgId = orgMember.getOrgId();
                        // 如果当前组织就是要切换到的组织，则直接返回true
                        if (StringUtils.equals(previousCurrentOrgId, nextCurrentOrgId)) {
                            return Mono.just(true);
                        }
                        // 移除之前的当前组织标记，并标记新的当前组织
                        return orgMemberService.removeCurrentOrgMark(previousCurrentOrgId, userId)
                                .flatMap(removeResult -> {
                                    if (removeResult) {
                                        return orgMemberService.markAsUserCurrentOrgId(nextCurrentOrgId, userId);
                                    }
                                    return Mono.error(new BizException(BizError.SWITCH_CURRENT_ORG_ERROR, "SWITCH_CURRENT_ORG_ERROR"));
                                });
                    }
                    // 如果之前没有当前组织，则直接标记新的当前组织
                    return orgMemberService.markAsUserCurrentOrgId(nextCurrentOrgId, userId);
                });
    }

    /**
     * 离开组织。
     *
     * @param orgId 组织ID。
     * @return 如果成功离开组织，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> leaveOrganization(String orgId) {
        return Mono.zip(sessionUserService.getVisitorId(), orgMemberService.getAllOrgAdmins(orgId))
                .flatMap(tuple -> {
                    String visitorId = tuple.getT1();
                    List<OrgMember> orgAdmins = tuple.getT2();
                    if (orgAdmins.size() == 1 && orgAdmins.get(0).getUserId().equals(visitorId)) {
                        return ofError(LAST_ADMIN_CANNOT_LEAVE_ORG, "LAST_ADMIN_CANNOT_LEAVE_ORG");
                    }
                    return orgMemberService.removeMember(orgId, visitorId)
                            .handle((result, sink) -> {
                                if (result) {
                                    applicationContext.publishEvent(new OrgMemberLeftEvent(orgId, visitorId));
                                    sink.next(true);
                                    return;
                                }
                                sink.next(false);
                            });
                });
    }

    /**
     * 删除组织的Logo。
     *
     * @param orgId 组织ID。
     * @return 如果成功删除组织的Logo，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> deleteLogo(String orgId) {
        return checkVisitorAdminRole(orgId)
                .then(organizationService.deleteLogo(orgId));
    }

    /**
     * 上传组织的Logo。
     *
     * @param orgId    组织ID。
     * @param fileMono 包含上传的文件的Mono。
     * @return 如果成功上传组织的Logo，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> uploadLogo(String orgId, Mono<Part> fileMono) {
        return checkVisitorAdminRole(orgId)
                .then(fileMono)
                .flatMap(file -> organizationService.uploadLogo(orgId, file));
    }

    /**
     * 从组织中移除指定用户，并在企业模式下标记用户为已删除。
     *
     * @param orgId  组织ID。
     * @param userId 用户ID。
     * @return 如果成功移除用户，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> removeUserFromOrg(String orgId, String userId) {
        return checkVisitorAdminRole(orgId)
                .then(orgMemberService.removeMember(orgId, userId))
                .doOnNext(result -> {
                    if (result) {
                        applicationContext.publishEvent(new OrgMemberLeftEvent(orgId, userId));
                    }
                })
                .delayUntil(__ -> userService.markUserDeletedAndInvalidConnectionsAtEnterpriseMode(userId));
    }

    /**
     * 删除组织。
     *
     * @param orgId 组织ID。
     * @return 如果成功删除组织，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> removeOrg(String orgId) {
        return checkVisitorAdminRole(orgId)
                .then(Mono.defer(() -> {
                    Workspace workspace = commonConfig.getWorkspace();
                    // 检查是否为企业模式下的主组织，如果是，则不允许删除
                    if (workspace.getMode() == WorkspaceMode.ENTERPRISE && orgId.equals(workspace.getEnterpriseOrgId())) {
                        return Mono.error(new BizException(UNSUPPORTED_OPERATION, "BAD_REQUEST"));
                    }
                    return Mono.empty();
                }))
                .then(organizationService.delete(orgId));
    }

    /**
     * 创建组织。
     *
     * @param organization 组织信息。
     * @return 创建的组织视图。
     */
    @Override
    public Mono<OrgView> create(Organization organization) {
        return sessionUserService.getVisitorId()
                .delayUntil(userId -> bizThresholdChecker.checkMaxOrgCount(userId))
                .delayUntil(__ -> checkIfSaasMode())
                .flatMap(userId -> organizationService.create(organization, userId))
                .map(OrgView::new);
    }

    /**
     * 更新组织信息。
     *
     * @param orgId            组织ID。
     * @param updateOrgRequest 更新组织的请求信息。
     * @return 如果成功更新组织信息，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> update(String orgId, UpdateOrgRequest updateOrgRequest) {
        return checkVisitorAdminRole(orgId)
                .flatMap(orgMember -> {
                    Organization updateOrg = new Organization();
                    updateOrg.setName(updateOrgRequest.getOrgName());
                    updateOrg.setContactEmail(updateOrgRequest.getContactEmail());
                    updateOrg.setContactPhoneNumber(updateOrgRequest.getContactPhoneNumber());
                    updateOrg.setContactName(updateOrgRequest.getContactName());
                    return organizationService.update(orgId, updateOrg);
                });
    }

    /**
     * 检查组织域名是否合法。
     *
     * @return 组织域名检查结果。
     */
    @Override
    public Mono<OrganizationDomainCheckResult> checkOrganizationDomain() {
        if (!commonConfig.isCloud()) {
            return Mono.just(OrganizationDomainCheckResult.success());
        }
        return sessionUserService.getVisitor()
                .flatMap(this::doCheckOrganizationDomain);
    }

    /**
     * 检查组织域名是否合法。
     *
     * @param user 访问用户。
     * @return 组织域名检查结果。
     */
    private Mono<OrganizationDomainCheckResult> doCheckOrganizationDomain(User user) {
        if (user.isAnonymous()) {
            return Mono.just(OrganizationDomainCheckResult.success());
        }

        return sessionUserService.getVisitorOrgMemberCache()
                .zipWhen(orgMember -> organizationService.getById(orgMember.getOrgId()))
                .zipWith(UriUtils.getRefererDomainFromContext())
                .flatMap(tuple -> {
                    String userId = tuple.getT1().getT1().getUserId();
                    Organization userCurrentOrg = tuple.getT1().getT2();
                    String currentRequestDomain = tuple.getT2();
                    String currentOrgDomain = Optional.ofNullable(userCurrentOrg.getOrganizationDomain())
                            .map(OrganizationDomain::getDomain)
                            .orElse(commonConfig.getDomain().getDefaultValue());
                    if (currentOrgDomain.equals("skipCheck")) {
                        return Mono.just(OrganizationDomainCheckResult.success());
                    }
                    // 域名匹配组织
                    if (currentRequestDomain.equalsIgnoreCase(currentOrgDomain)) {
                        return Mono.just(OrganizationDomainCheckResult.success());
                    }
                    // 域名与组织不匹配
                    return dealWithMismatchDomain(currentRequestDomain, userId, currentOrgDomain);
                })
                .defaultIfEmpty(OrganizationDomainCheckResult.success());
    }

    /**
     * 处理域名不匹配的情况。
     *
     * @param requestDomain    请求的域名。
     * @param userId           用户ID。
     * @param currentOrgDomain 当前组织的域名。
     * @return 处理结果。
     */
    private Mono<OrganizationDomainCheckResult> dealWithMismatchDomain(String requestDomain, String userId, String currentOrgDomain) {
        return organizationService.getByDomain()
                .flatMap(requestDomainOrg -> dealWithOrganizationDomain(userId, currentOrgDomain, requestDomainOrg))
                .defaultIfEmpty(OrganizationDomainCheckResult.redirect(currentOrgDomain));
    }

    /**
     * 处理组织域名不匹配的情况。
     *
     * @param userId           用户ID。
     * @param currentOrgDomain 当前组织的域名。
     * @param requestDomainOrg 请求的组织域。
     * @return 处理结果。
     */
    private Mono<OrganizationDomainCheckResult> dealWithOrganizationDomain(String userId, String currentOrgDomain, Organization requestDomainOrg) {
        return orgMemberService.getOrgMember(requestDomainOrg.getId(), userId)
                .flatMap(orgMember -> Mono.just(OrganizationDomainCheckResult.redirect(currentOrgDomain)))
                .defaultIfEmpty(OrganizationDomainCheckResult.bind());
    }

    /**
     * 获取组织的通用设置。
     *
     * @param orgId 组织ID。
     * @return 组织的通用设置。
     */
    @Override
    public Mono<OrganizationCommonSettings> getOrgCommonSettings(String orgId) {
        return sessionUserService.getVisitorId()
                .flatMap(visitor -> orgMemberService.getOrgMember(orgId, visitor))
                .switchIfEmpty(deferredError(BizError.NOT_AUTHORIZED, "NOT_AUTHORIZED"))
                .flatMap(it -> organizationService.getById(orgId))
                .map(Organization::getCommonSettings);
    }

    /**
     * 更新组织的通用设置。
     *
     * @param orgId 组织ID。
     * @param key   设置键。
     * @param value 设置值。
     * @return 如果成功更新通用设置，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> updateOrgCommonSettings(String orgId, String key, Object value) {
        return checkVisitorAdminRole(orgId)
                .flatMap(__ -> organizationService.updateCommonSettings(orgId, key, value));
    }

    /**
     * 尝试将用户添加到组织并切换组织。
     *
     * @param orgId  组织ID。
     * @param userId 用户ID。
     * @return 如果成功添加用户到组织并切换组织，则返回true；否则返回false。
     */
    @Override
    public Mono<Boolean> tryAddUserToOrgAndSwitchOrg(String orgId, String userId) {
        return orgMemberService.tryAddOrgMember(orgId, userId, MemberRole.MEMBER)
                .then(switchCurrentOrganizationTo(orgId));
    }

    /**
     * 获取组织配置信息。
     *
     * @return 组织配置信息视图。
     */
    @Override
    public Mono<ConfigView> getOrganizationConfigs() {
        Mono<Organization> organizationMono = this.organizationService.getByDomain().cache();
        Mono<Map<String, Object>> brandingMono = organizationMono
                .switchIfEmpty(this.organizationService.getOrganizationInEnterpriseMode())
                .map(organization -> Optional.ofNullable(organization.getCommonSettings().get("branding"))
                        .map(value -> (Map<String, Object>) value)
                        .orElseGet(HashMap::new))
                .defaultIfEmpty(new HashMap<>());
        return authenticationService.findAllAuthConfigs(true)
                .map(FindAuthConfig::authConfig)
                .collectList()
                .zipWith(organizationMono.hasElement())
                .flatMap(tuple -> {
                    List<AbstractAuthConfig> authConfigs = tuple.getT1();
                    Boolean hasSelfDomain = tuple.getT2();
                    return brandingMono.map(branding -> {
                        authConfigs.forEach(authConfig -> {
                            if (authConfig instanceof EmailAuthConfig && authConfig.getSourceName().equals("EMAIL")) {
                                ((EmailAuthConfig) authConfig).setPublicKey(rsacryptoService.publicKeyString);
                            }
                        });
                        return ConfigView.builder()
                                .branding(branding)
                                .product(commonConfig.getProduct())
                                .authConfigs(authConfigs)
                                .isCloudHosting(commonConfig.isCloud())
                                .workspaceMode(commonConfig.getWorkspace().getMode())
                                .selfDomain(hasSelfDomain)
                                .cookieName(commonConfig.getCookieName())
                                .build();
                    });
                });
    }

    /**
     * 检查是否处于SaaS模式。
     *
     * @return 如果是SaaS模式，则返回空；否则抛出不支持的操作异常。
     */
    private Mono<Void> checkIfSaasMode() {
        return Mono.defer(() -> {
            if (commonConfig.getWorkspace().getMode() == WorkspaceMode.ENTERPRISE) {
                return Mono.error(new BizException(UNSUPPORTED_OPERATION, "BAD_REQUEST"));
            }
            return Mono.empty();
        });
    }
}
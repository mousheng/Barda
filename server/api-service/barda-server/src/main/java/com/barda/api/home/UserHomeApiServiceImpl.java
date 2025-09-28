package com.barda.api.home;

import static com.barda.domain.permission.model.ResourceAction.READ_APPLICATIONS;
import static com.barda.infra.util.MonoUtils.emptyIfNull;
import static com.barda.sdk.util.StreamUtils.collectList;
import static java.util.Objects.isNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.barda.api.application.view.ApplicationInfoView;
import com.barda.api.application.view.ApplicationInfoView.ApplicationInfoViewBuilder;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.api.usermanagement.view.OrgAndVisitorRoleView;
import com.barda.api.usermanagement.view.UserProfileView;
import com.barda.domain.application.model.Application;
import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.model.ApplicationType;
import com.barda.domain.application.service.ApplicationService;
import com.barda.domain.interaction.UserApplicationInteraction;
import com.barda.domain.interaction.UserApplicationInteractionService;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.organization.service.OrganizationService;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.user.model.User;
import com.barda.domain.user.model.UserStatus;
import com.barda.domain.user.service.UserService;
import com.barda.domain.user.service.UserStatusService;
import com.barda.infra.util.NetworkUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * 实现了UserHomeApiService接口，提供用户主页相关的功能。
 */
@Component
public class UserHomeApiServiceImpl implements UserHomeApiService {

    /**
     * 用于获取当前会话的用户信息。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 用于操作组织机构相关的功能。
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * 用于操作组织成员相关的功能。
     */
    @Autowired
    private OrgMemberService orgMemberService;

    /**
     * 用于操作应用相关的功能。
     */
    @Autowired
    private ApplicationService applicationService;

    /**
     * 用于操作资源权限相关的功能。
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 用于操作用户相关的功能。
     */
    @Autowired
    private UserService userService;

    /**
     * 用于操作用户状态相关的功能。
     */
    @Autowired
    private UserStatusService userStatusService;

    /**
     * 用于检查当前组织是否为开发者组织。
     */
    @Autowired
    private OrgDevChecker orgDevChecker;

    /**
     * 用于操作文件夹相关的功能。
     */
    @Autowired
    private FolderApiService folderApiService;

    /**
     * 用于操作用户应用交互相关的功能。
     */
    @Autowired
    private UserApplicationInteractionService userApplicationInteractionService;

    /**
     * 构建用户个人资料视图。
     *
     * @param user      当前用户
     * @param exchange  用于获取客户端IP
     * @return          用户个人资料视图
     */
    @Override
    public Mono<UserProfileView> buildUserProfileView(User user, ServerWebExchange exchange) {
        // 若为匿名用户，返回匿名用户的个人资料视图
        if (user.isAnonymous()) {
            return Mono.just(UserProfileView.builder()
                    .isAnonymous(true)
                    .username(user.getName())
                    .ip(NetworkUtils.getRemoteIp(exchange))
                    .build()
            );
        }

        // 获取用户状态
        Mono<UserStatus> userStatusMono = userStatusService.findByUserId(user.getId());

        // 获取用户在不同组织中的信息
        return Mono.zip(userStatusMono, orgMemberService.getUserOrgMemberInfo(user.getId()))
                .flatMap(tuple -> {
                    UserStatus userStatus = tuple.getT1();
                    OrgMember currentOrgMember = tuple.getT2().currentOrgMember();
                    List<OrgMember> orgMembers = tuple.getT2().orgMembers();
                    List<String> orgIds = collectList(orgMembers, OrgMember::getOrgId);
                    // 获取组织机构和访客角色信息
                    Mono<List<OrgAndVisitorRoleView>> orgAndRolesMono = organizationService.getByIds(orgIds)
                            .collectMap(Organization::getId, Function.identity())
                            .map(map -> orgMembers.stream()
                                    .map(member -> {
                                        String orgId = member.getOrgId();
                                        Organization organization = map.get(orgId);
                                        if (organization == null) {
                                            return null;
                                        }
                                        return new OrgAndVisitorRoleView(organization, member.getRole().getValue());
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList()));

                    String currentOrgId = currentOrgMember.getOrgId();

                    // 获取当前组织是否为开发者组织
                    return Mono.zip(orgAndRolesMono, orgDevChecker.isCurrentOrgDev())
                            .map(tuple2 -> {
                                List<OrgAndVisitorRoleView> orgAndRoles = tuple2.getT1();
                                boolean isOrgDev = tuple2.getT2();
                                return UserProfileView.builder()
                                        .id(user.getId())
                                        .username(user.getName())
                                        .isAnonymous(user.isAnonymous())
                                        .avatarUrl(user.getAvatarUrl())
                                        .avatar(user.getAvatar())
                                        .connections(user.getConnections())
                                        .currentOrgId(currentOrgId)
                                        .orgAndRoles(orgAndRoles)
                                        .hasPassword(StringUtils.isNotBlank(user.getPassword()))
                                        .hasSetNickname(user.isHasSetNickname())
                                        .userStatus(userStatus.getStatusMap())
                                        .isOrgDev(isOrgDev)
                                        .createdTimeMs(user.getCreatedAt().toEpochMilli())
                                        .ip(NetworkUtils.getRemoteIp(exchange))
                                        .build();
                            });
                });
    }

    /**
     * 标记新用户引导已展示。
     * @param userId    用户ID
     * @return          标记是否成功
     */
    @Override
    public Mono<Boolean> markNewUserGuidanceShown(String userId) {
        return userStatusService.markNewUserGuidanceShown(userId);
    }

    /**
     * 获取用户主页视图。
     *
     * @param applicationType  应用类型
     * @return                  用户主页视图
     */
    public Mono<UserHomepageView> getUserHomePageView(ApplicationType applicationType) {
        // 获取访客信息
        Mono<User> userMono = sessionUserService.getVisitor();

        // 获取访客在当前组织中的信息
        Mono<String> currentOrgIdMono = sessionUserService.getVisitorOrgMemberCache()
                .map(OrgMember::getOrgId);

        return Mono.zip(userMono, currentOrgIdMono)
                .flatMap(tuple -> {
                    User user = tuple.getT1();
                    String currentOrgId = tuple.getT2();

                    UserHomepageView userHomepageVO = new UserHomepageView();
                    userHomepageVO.setUser(user);

                    // 若访客不在任何组织中，返回空的用户主页视图
                    if (StringUtils.isBlank(currentOrgId)) {
                        return Mono.just(userHomepageVO);
                    }

                    // 获取当前组织信息和访客在该组织中的应用和文件夹
                    return organizationService.getById(currentOrgId)
                            .zipWith(folderApiService.getElements(null, applicationType).collectList())
                            .map(tuple2 -> {
                                Organization organization = tuple2.getT1();
                                List<?> list = tuple2.getT2();
                                List<ApplicationInfoView> applicationInfoViews = list.stream()
                                        .map(o -> {
                                            if (o instanceof ApplicationInfoView applicationInfoView) {
                                                return applicationInfoView;
                                            }
                                            return null;
                                        })
                                        .filter(Objects::nonNull)
                                        .toList();
                                List<FolderInfoView> folderInfoViews = list.stream()
                                        .map(o -> {
                                            if (o instanceof FolderInfoView folderInfoView) {
                                                return folderInfoView;
                                            }
                                            return null;
                                        })
                                        .filter(Objects::nonNull)
                                        .toList();
                                userHomepageVO.setOrganization(organization);
                                userHomepageVO.setHomeApplicationViews(applicationInfoViews);
                                userHomepageVO.setFolderInfoViews(folderInfoViews);
                                return userHomepageVO;
                            });
                });
    }

    /**
     * 获取当前访客在当前组织中的所有授权应用。
     *
     * @param applicationType      应用类型
     * @param applicationStatus    应用状态
     * @param withContainerSize    是否返回应用容器大小
     * @return                      应用信息视图流
     */
    @Override
    public Flux<ApplicationInfoView> getAllAuthorisedApplications4CurrentOrgMember(@Nullable ApplicationType applicationType,
            @Nullable ApplicationStatus applicationStatus, boolean withContainerSize) {

        return sessionUserService.getVisitorOrgMemberCache()
                .flatMapMany(orgMember -> {
                    String visitorId = orgMember.getUserId();
                    String currentOrgId = orgMember.getOrgId();
                    // 获取应用流
                    Flux<Application> applicationFlux = Flux.defer(() -> {
                                if (withContainerSize) {
                                    return applicationService.findByOrganizationIdWithDsl(currentOrgId);
                                }
                                return applicationService.findByOrganizationIdWithoutDsl(currentOrgId);
                            })
                            .filter(application -> isNull(applicationType) || application.getApplicationType() == applicationType.getValue())
                            .filter(application -> isNull(applicationStatus) || application.getApplicationStatus() == applicationStatus)
                            .cache()
                            .collectList()
                            .flatMapIterable(Function.identity());

                    // 获取应用最近查看时间
                    Mono<Map<String, Instant>> applicationLastViewTimeMapMono = userApplicationInteractionService.findByUserId(visitorId)
                            .collectMap(UserApplicationInteraction::applicationId, UserApplicationInteraction::lastViewTime)
                            .cache();

                    // 获取应用的最大匹配权限
                    Mono<Map<String, ResourcePermission>> resourcePermissionMapMono = applicationFlux
                            .mapNotNull(Application::getId)
                            .collectList()
                            .flatMap(applicationIds -> resourcePermissionService.getMaxMatchingPermission(visitorId, applicationIds,
                                    READ_APPLICATIONS))
                            .cache();

                    // 获取应用创建者信息
                    Mono<Map<String, User>> userMapMono = applicationFlux
                            .flatMap(application -> emptyIfNull(application.getCreatedBy()))
                            .collectList()
                            .flatMap(creatorIds -> userService.getByIds(creatorIds))
                            .cache();

                    return applicationFlux
                            .flatMap(application -> Mono.zip(Mono.just(application), resourcePermissionMapMono, userMapMono,
                                    applicationLastViewTimeMapMono))
                            .filter(tuple -> {
                                // 按权限过滤
                                Application application = tuple.getT1();
                                Map<String, ResourcePermission> resourcePermissionMap = tuple.getT2();
                                return resourcePermissionMap.containsKey(application.getId());
                            })
                            .map(tuple -> {
                                // 构建应用信息视图
                                Application application = tuple.getT1();
                                Map<String, ResourcePermission> resourcePermissionMap = tuple.getT2();
                                Map<String, User> userMap = tuple.getT3();
                                Map<String, Instant> applicationLastViewTimeMap = tuple.getT4();
                                ResourceRole resourceRole = resourcePermissionMap.get(application.getId()).getResourceRole();
                                return buildView(application, resourceRole, userMap, applicationLastViewTimeMap.get(application.getId()),
                                        withContainerSize);
                            });
                });
    }

    /**
     * 构建应用程序信息视图。
     *
     * @param application 应用程序对象。
     * @param maxRole 用户的最大资源角色。
     * @param userMap 用户ID到用户对象的映射。
     * @param lastViewTime 上次查看时间，可以为null。
     * @param withContainerSize 是否包含容器大小信息。
     * @return 应用程序信息视图对象。
     */
    private ApplicationInfoView buildView(Application application, ResourceRole maxRole, Map<String, User> userMap, @Nullable Instant lastViewTime,
            boolean withContainerSize) {
        ApplicationInfoViewBuilder applicationInfoViewBuilder = ApplicationInfoView.builder()
                .applicationId(application.getId())
                .orgId(application.getOrganizationId())
                .name(application.getName())
                .createBy(Optional.ofNullable(userMap.get(application.getCreatedBy()))
                        .map(User::getName)
                        .orElse(""))
                .createAt(application.getCreatedAt().toEpochMilli())
                .role(maxRole.getValue())
                .applicationType(application.getApplicationType())
                .applicationStatus(application.getApplicationStatus())
                .lastModifyTime(application.getUpdatedAt())
                .lastViewTime(lastViewTime)
                .publicToAll(application.isPublicToAll());
        if (withContainerSize) {
            return applicationInfoViewBuilder
                    .containerSize(application.getLiveContainerSize())
                    .build();
        }
        return applicationInfoViewBuilder.build();
    }
}

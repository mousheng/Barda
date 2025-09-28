package com.barda.api.application;

import static com.barda.domain.application.model.ApplicationStatus.NORMAL;
import static com.barda.domain.permission.model.ResourceAction.EDIT_APPLICATIONS;
import static com.barda.domain.permission.model.ResourceAction.MANAGE_APPLICATIONS;
import static com.barda.domain.permission.model.ResourceAction.PUBLISH_APPLICATIONS;
import static com.barda.domain.permission.model.ResourceAction.READ_APPLICATIONS;
import static com.barda.domain.permission.model.ResourceAction.USE_DATASOURCES;
import static com.barda.sdk.exception.BizError.ILLEGAL_APPLICATION_PERMISSION_ID;
import static com.barda.sdk.exception.BizError.INVALID_PARAMETER;
import static com.barda.sdk.exception.BizError.NOT_AUTHORIZED;
import static com.barda.sdk.exception.BizError.NO_PERMISSION_TO_REQUEST_APP;
import static com.barda.sdk.exception.BizError.USER_NOT_SIGNED_IN;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.barda.api.application.ApplicationController.CreateApplicationRequest;
import com.barda.api.application.view.ApplicationInfoView;
import com.barda.api.application.view.ApplicationPermissionView;
import com.barda.api.application.view.ApplicationView;
import com.barda.api.bizthreshold.AbstractBizThresholdChecker;
import com.barda.api.home.FolderApiService;
import com.barda.api.home.SessionUserService;
import com.barda.api.home.UserHomeApiService;
import com.barda.api.permission.PermissionHelper;
import com.barda.api.permission.view.PermissionItemView;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.domain.application.model.Application;
import com.barda.domain.application.model.ApplicationStatus;
import com.barda.domain.application.model.ApplicationType;
import com.barda.domain.application.service.ApplicationService;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.group.service.GroupService;
import com.barda.domain.interaction.UserApplicationInteractionService;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.organization.service.OrganizationService;
import com.barda.domain.permission.model.ResourceAction;
import com.barda.domain.permission.model.ResourceHolder;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.model.ResourceType;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.permission.solution.SuggestAppAdminSolution;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.domain.solutions.TemplateSolution;
import com.barda.domain.template.model.Template;
import com.barda.domain.template.service.TemplateService;
import com.barda.domain.user.service.UserService;
import com.barda.infra.util.TupleUtils;
import com.barda.sdk.constants.Authentication;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.plugin.common.QueryExecutor;
import com.barda.sdk.util.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 应用相关的API服务。
 */
@Service
@Slf4j
public class ApplicationApiService {

    private static final String LIBRARY_QUERY_DATASOURCE_TYPE = "libraryQuery";
    private static final String JS_DATASOURCE_TYPE = "js";
    private static final String VIEW_DATASOURCE_TYPE = "view";

    /**
     * 应用服务。
     */
    @Autowired
    private ApplicationService applicationService;

    /**
     * 资源权限服务。
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 会话用户服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

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
     * 组织服务。
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * 用户服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 业务阈值检查器。
     */
    @Autowired
    private AbstractBizThresholdChecker bizThresholdChecker;

    /**
     * 模板解决方案。
     */
    @Autowired
    private TemplateSolution templateSolution;

    /**
     * 建议应用管理员解决方案。
     */
    @Autowired
    private SuggestAppAdminSolution suggestAppAdminSolution;

    /**
     * 组织开发检查器。
     */
    @Autowired
    private OrgDevChecker orgDevChecker;

    /**
     * 文件夹API服务。
     */
    @Autowired
    private FolderApiService folderApiService;

    /**
     * 用户主页API服务。
     */
    @Autowired
    private UserHomeApiService userHomeApiService;

    /**
     * 用户应用交互服务。
     */
    @Autowired
    private UserApplicationInteractionService userApplicationInteractionService;

    /**
     * 数据源元信息服务。
     */
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    /**
     * 复合应用DSL过滤器。
     */
    @Autowired
    private CompoundApplicationDslFilter compoundApplicationDslFilter;

    /**
     * 模板服务。
     */
    @Autowired
    private TemplateService templateService;

    /**
     * 权限帮助器。
     */
    @Autowired
    private PermissionHelper permissionHelper;

    /**
     * 数据源服务。
     */
    @Autowired
    private DatasourceService datasourceService;

    /**
     * 创建一个新的应用。
     *
     * @param createApplicationRequest 应用创建请求
     * @return 应用视图的Mono
     */
    public Mono<ApplicationView> create(CreateApplicationRequest createApplicationRequest) {
        // 创建一个新的应用
        Application application = new Application(createApplicationRequest.organizationId(),
                createApplicationRequest.name(),
                createApplicationRequest.applicationType(),
                NORMAL,
                createApplicationRequest.publishedApplicationDSL(),
                false, createApplicationRequest.editingApplicationDSL());
        // 验证组织ID是否为空
        if (StringUtils.isBlank(application.getOrganizationId())) {
            return deferredError(INVALID_PARAMETER, "ORG_ID_EMPTY");
        }
        // 验证应用名称是否为空
        if (StringUtils.isBlank(application.getName())) {
            return deferredError(INVALID_PARAMETER, "APP_NAME_EMPTY");
        }
        // 获取访客ID并检查组织成员
        return sessionUserService.getVisitorId()
                .flatMap(userId -> orgMemberService.getOrgMember(application.getOrganizationId(), userId))
                .switchIfEmpty(deferredError(NOT_AUTHORIZED, "NOT_AUTHORIZED"))
                .delayUntil(orgMember -> orgDevChecker.checkCurrentOrgDev())
                .delayUntil(bizThresholdChecker::checkMaxOrgApplicationCount)
                .delayUntil(orgMember -> {
                    String folderId = createApplicationRequest.folderId();
                    if (StringUtils.isBlank(folderId)) {
                        return Mono.empty();
                    }
                    // 检查文件夹是否存在并检查文件夹是否属于当前组织
                    return folderApiService.checkFolderExist(folderId)
                            .flatMap(folder -> folderApiService.checkFolderCurrentOrg(folder, orgMember.getOrgId()));
                })
                .flatMap(org -> applicationService.create(application, org.getUserId()))
                .delayUntil(created -> autoGrantPermissionsByFolderDefault(created.getId(), createApplicationRequest.folderId()))
                .delayUntil(created -> folderApiService.move(created.getId(),
                        createApplicationRequest.folderId()))
                .map(applicationCreated -> ApplicationView.builder()
                        .applicationInfoView(buildView(applicationCreated, "", createApplicationRequest.folderId()))
                        .applicationDSL(applicationCreated.getEditingApplicationDSL())
                        .build());
    }

    /**
     * 基于文件夹的默认权限自动授权。
     *
     * @param applicationId 应用ID
     * @param folderId      文件夹ID
     * @return 空的Mono
     */
    private Mono<Void> autoGrantPermissionsByFolderDefault(String applicationId, @Nullable String folderId) {
        // 如果文件夹ID为空，返回空的Mono
        if (StringUtils.isBlank(folderId)) {
            return Mono.empty();
        }
        // 获取文件夹的权限
        return folderApiService.getPermissions(folderId)
                .flatMapIterable(ApplicationPermissionView::getPermissions)
                .groupBy(PermissionItemView::getRole)
                .flatMap(sameRolePermissionItemViewFlux -> {
                    String role = sameRolePermissionItemViewFlux.key();
                    Flux<PermissionItemView> permissionItemViewFlux = sameRolePermissionItemViewFlux.cache();
                    // 获取拥有该角色的用户ID列表
                    Mono<List<String>> userIdsMono = permissionItemViewFlux
                            .filter(permissionItemView -> permissionItemView.getType() == ResourceHolder.USER)
                            .map(PermissionItemView::getId)
                            .collectList();
                    // 获取拥有该角色的组ID列表
                    Mono<List<String>> groupIdsMono = permissionItemViewFlux
                            .filter(permissionItemView -> permissionItemView.getType() == ResourceHolder.GROUP)
                            .map(PermissionItemView::getId)
                            .collectList();
                    // 合并用户ID列表和组ID列表，并插入到应用的权限中
                    return Mono.zip(userIdsMono, groupIdsMono)
                            .flatMap(tuple -> {
                                List<String> userIds = tuple.getT1();
                                List<String> groupIds = tuple.getT2();
                                return resourcePermissionService.insertBatchPermission(ResourceType.APPLICATION, applicationId,
                                        new HashSet<>(userIds), new HashSet<>(groupIds),
                                        ResourceRole.fromValue(role));
                            });
                })
                .then(); // 所有操作完成后返回空的Mono
    }

    /**
     * 获取回收站中的应用列表。
     *
     * @return 应用信息视图的Flux
     */
    public Flux<ApplicationInfoView> getRecycledApplications() {
        return userHomeApiService.getAllAuthorisedApplications4CurrentOrgMember(null, ApplicationStatus.RECYCLED, false);
    }

    /**
     * 检查当前用户对应用的操作权限。
     *
     * @param applicationId 应用ID
     * @param action        操作
     * @return 空的Mono
     */
    private Mono<Void> checkCurrentUserApplicationPermission(String applicationId, ResourceAction action) {
        return sessionUserService.getVisitorId()
                .flatMap(userId -> resourcePermissionService.checkResourcePermissionWithError(userId, applicationId, action));
    }

    /**
     * 删除应用。
     *
     * @param applicationId 应用ID
     * @return 应用视图的Mono
     */
    public Mono<ApplicationView> delete(String applicationId) {
        return checkApplicationStatus(applicationId, ApplicationStatus.RECYCLED)
                .then(updateApplicationStatus(applicationId, ApplicationStatus.DELETED))
                .then(applicationService.findById(applicationId))
                .map(application -> ApplicationView.builder()
                        .applicationInfoView(buildView(application))
                        .applicationDSL(application.getEditingApplicationDSL())
                        .build());
    }

    /**
     * 回收应用。
     *
     * @param applicationId 应用ID
     * @return 布尔值的Mono
     */
    public Mono<Boolean> recycle(String applicationId) {
        return checkApplicationStatus(applicationId, NORMAL)
                .then(updateApplicationStatus(applicationId, ApplicationStatus.RECYCLED));
    }

    /**
     * 恢复应用。
     *
     * @param applicationId 应用ID
     * @return 布尔值的Mono
     */
    public Mono<Boolean> restore(String applicationId) {
        return checkApplicationStatus(applicationId, ApplicationStatus.RECYCLED)
                .then(updateApplicationStatus(applicationId, NORMAL));
    }

    /**
     * 检查应用的状态。
     *
     * @param applicationId 应用ID
     * @param expected      期望的应用状态
     * @return 空的Mono
     */
    private Mono<Void> checkApplicationStatus(String applicationId, ApplicationStatus expected) {
        return applicationService.findByIdWithoutDsl(applicationId)
                .flatMap(application -> checkApplicationStatus(application, expected));
    }

    /**
     * 检查应用的状态。
     *
     * @param application 应用
     * @param expected    期望的应用状态
     * @return 空的Mono
     */
    private Mono<Void> checkApplicationStatus(Application application, ApplicationStatus expected) {
        if (expected == application.getApplicationStatus()) {
            return Mono.empty();
        }
        return Mono.error(new BizException(BizError.UNSUPPORTED_OPERATION, "BAD_REQUEST"));
    }

    /**
     * 更新应用的状态。
     *
     * @param applicationId     应用ID
     * @param applicationStatus 应用状态
     * @return 布尔值的Mono
     */
    private Mono<Boolean> updateApplicationStatus(String applicationId, ApplicationStatus applicationStatus) {
        return checkCurrentUserApplicationPermission(applicationId, MANAGE_APPLICATIONS)
                .then(Mono.defer(() -> {
                    Application application = Application.builder()
                            .applicationStatus(applicationStatus)
                            .build();
                    return applicationService.updateById(applicationId, application);
                }));
    }

    /**
     * 获取编辑中的应用。
     *
     * @param applicationId 应用ID
     * @return 应用视图Mono
     */
    public Mono<ApplicationView> getEditingApplication(String applicationId) {
        // 1. 校验用户对应用的编辑权限并返回错误信息
        // 2. 并行执行以下任务：
        //    a. 获取应用信息
        //    b. 获取应用的依赖模块
        //    c. 获取应用所属组织的通用设置
        // 3. 合并任务结果并构建应用视图
        return checkPermissionWithReadableErrorMsg(applicationId, EDIT_APPLICATIONS)
                .zipWhen(permission -> applicationService.findById(applicationId)
                        .delayUntil(application -> checkApplicationStatus(application, NORMAL)))
                .zipWhen(tuple -> applicationService.getAllDependentModulesFromApplication(tuple.getT2(), false), TupleUtils::merge)
                .zipWhen(tuple -> organizationService.getOrgCommonSettings(tuple.getT2().getOrganizationId()), TupleUtils::merge)
                .map(tuple -> {
                    ResourcePermission permission = tuple.getT1();
                    Application application = tuple.getT2();
                    List<Application> dependentModules = tuple.getT3();
                    Map<String, Object> commonSettings = tuple.getT4();
                    // 构建应用的依赖模块DSL
                    Map<String, Map<String, Object>> dependentModuleDsl = dependentModules.stream()
                            .collect(Collectors.toMap(Application::getId, Application::getLiveApplicationDsl, (a, b) -> b));
                    // 构建应用视图
                    return ApplicationView.builder()
                            .applicationInfoView(buildView(application, permission.getResourceRole().getValue()))
                            .applicationDSL(application.getEditingApplicationDSL())
                            .moduleDSL(dependentModuleDsl)
                            .orgCommonSettings(commonSettings)
                            .build();
                });
    }

    /**
     * 获取已发布的应用。
     *
     * @param applicationId 应用ID
     * @return 应用视图Mono
     */
    public Mono<ApplicationView> getPublishedApplication(String applicationId) {
        // 1. 校验用户对应用的读取权限并返回错误信息
        // 2. 并行执行以下任务：
        //    a. 获取应用信息
        //    b. 获取应用的依赖模块
        //    c. 获取应用所属组织的通用设置
        //    d. 获取应用的模板ID
        // 3. 合并任务结果并构建应用视图
        // 4. 如果应用类型是复合应用，则从复合应用DSL中移除子应用
        return checkPermissionWithReadableErrorMsg(applicationId, READ_APPLICATIONS)
                .zipWhen(permission -> applicationService.findById(applicationId)
                        .delayUntil(application -> checkApplicationStatus(application, NORMAL)))
                .zipWhen(tuple -> applicationService.getAllDependentModulesFromApplication(tuple.getT2(), true), TupleUtils::merge)
                .zipWhen(tuple -> organizationService.getOrgCommonSettings(tuple.getT2().getOrganizationId()), TupleUtils::merge)
                .zipWith(getTemplateIdFromApplicationId(applicationId), TupleUtils::merge)
                .map(tuple -> {
                    ResourcePermission permission = tuple.getT1();
                    Application application = tuple.getT2();
                    List<Application> dependentModules = tuple.getT3();
                    Map<String, Object> commonSettings = tuple.getT4();
                    String templateId = tuple.getT5();

                    // 构建应用的依赖模块DSL
                    Map<String, Map<String, Object>> dependentModuleDsl = dependentModules.stream()
                            .collect(Collectors.toMap(Application::getId, app -> sanitizeDsl(app.getLiveApplicationDsl()), (a, b) -> b));

                    // 构建应用视图
                    return ApplicationView.builder()
                            .applicationInfoView(buildView(application, permission.getResourceRole().getValue()))
                            .applicationDSL(sanitizeDsl(application.getLiveApplicationDsl()))
                            .moduleDSL(dependentModuleDsl)
                            .orgCommonSettings(commonSettings)
                            .templateId(templateId)
                            .build();
                })
                .delayUntil(applicationView -> {
                    if (applicationView.getApplicationInfoView().getApplicationType() == ApplicationType.COMPOUND_APPLICATION.getValue()) {
                        return compoundApplicationDslFilter.removeSubAppsFromCompoundDsl(applicationView.getApplicationDSL());
                    }
                    return Mono.empty();
                });
    }

    /**
     * 获取应用的模板ID。
     *
     * @param applicationId 应用ID
     * @return 模板ID Mono
     */
    private Mono<String> getTemplateIdFromApplicationId(String applicationId) {
        // 1. 通过应用ID从模板服务中获取模板
        // 2. 如果模板存在，则返回模板ID
        // 3. 如果模板不存在，则返回空字符串
        // 4. 如果在获取模板时发生错误，则记录错误并返回空字符串
        return templateService.getByApplicationId(applicationId)
                .map(Template::getId)
                .defaultIfEmpty("")
                .onErrorResume(e -> {
                    log.error("获取应用的模板ID时发生错误", e);
                    return Mono.just("");
                });
    }

    /**
     * 更新用户对应用的最后查看时间。
     *
     * @param applicationId 应用ID
     * @return 空的Mono
     */
    public Mono<Void> updateUserApplicationLastViewTime(String applicationId) {
        // 1. 获取访客ID
        // 2. 如果访客ID不是匿名用户，则执行以下任务：
        //    a. 在用户应用交互服务中插入或更新记录
        // 3. 如果在更新记录时发生错误，则记录错误并返回空的Mono
        return sessionUserService.getVisitorId()
                .filter(Authentication::isNotAnonymousUser)
                .flatMap(visitorId -> userApplicationInteractionService.upsert(visitorId, applicationId, Instant.now()))
                .onErrorResume(throwable -> {
                    log.error("更新用户对应用的最后查看时间时发生错误", throwable);
                    return Mono.empty();
                });
    }

    /**
     * 更新应用。
     *
     * @param applicationId 应用ID
     * @param application   应用信息
     * @return 应用视图Mono
     */
    public Mono<ApplicationView> update(String applicationId, Application application) {
        // 1. 校验应用状态是否为正常
        // 2. 获取访客ID
        // 3. 校验访客ID是否有编辑应用的权限
        // 4. 校验应用的数据源权限
        // 5. 执行应用更新操作
        // 6. 构建并返回应用视图
        return checkApplicationStatus(applicationId, NORMAL)
                .then(sessionUserService.getVisitorId())
                .flatMap(userId -> resourcePermissionService.checkAndReturnMaxPermission(userId,
                        applicationId, EDIT_APPLICATIONS))
                .delayUntil(__ -> checkDatasourcePermissions(application))
                .flatMap(permission -> doUpdateApplication(applicationId, application)
                        .map(applicationUpdated -> ApplicationView.builder()
                                .applicationInfoView(buildView(applicationUpdated, permission.getResourceRole().getValue()))
                                .applicationDSL(applicationUpdated.getEditingApplicationDSL())
                                .build()));
    }

    /**
     * 执行应用更新操作。
     *
     * @param applicationId 应用ID
     * @param application   应用信息
     * @return 应用Mono
     */
    private Mono<Application> doUpdateApplication(String applicationId, Application application) {
        // 1. 创建应用更新信息
        // 2. 更新应用
        // 3. 获取更新后的应用信息
        Application applicationUpdate = Application.builder()
                .editingApplicationDSL(application.getEditingApplicationDSL())
                .name(application.getName())
                .build();
        return applicationService.updateById(applicationId, applicationUpdate)
                .then(applicationService.findById(applicationId));
    }

    /**
     * 发布应用。
     *
     * @param applicationId 应用ID
     * @return 应用视图Mono
     */
    public Mono<ApplicationView> publish(String applicationId) {
        // 1. 校验应用状态是否为正常
        // 2. 获取访客ID
        // 3. 校验访客ID是否有发布应用的权限
        // 4. 发布应用
        // 5. 构建并返回应用视图
        return checkApplicationStatus(applicationId, NORMAL)
                .then(sessionUserService.getVisitorId())
                .flatMap(userId -> resourcePermissionService.checkAndReturnMaxPermission(userId,
                        applicationId, PUBLISH_APPLICATIONS))
                .flatMap(permission -> applicationService.publish(applicationId)
                        .map(applicationUpdated -> ApplicationView.builder()
                                .applicationInfoView(buildView(applicationUpdated, permission.getResourceRole().getValue()))
                                .applicationDSL(applicationUpdated.getLiveApplicationDsl())
                                .build()));
    }

    /**
     * 为应用授予权限。
     *
     * @param applicationId 应用ID
     * @param userIds       要授予权限的用户ID集合
     * @param groupIds      要授予权限的组ID集合
     * @param role          要授予的权限角色
     * @return 授予权限是否成功的Mono
     */
    public Mono<Boolean> grantPermission(String applicationId,
                                         Set<String> userIds,
                                         Set<String> groupIds, ResourceRole role) {
        if (userIds.isEmpty() && groupIds.isEmpty()) {
            return Mono.just(true);
        }

        return checkCurrentUserApplicationPermission(applicationId, MANAGE_APPLICATIONS)
                .then(applicationService.findByIdWithoutDsl(applicationId))
                .delayUntil(application -> checkApplicationStatus(application, NORMAL))
                .switchIfEmpty(deferredError(BizError.APPLICATION_NOT_FOUND, "APPLICATION_NOT_FOUND", applicationId))
                .then(resourcePermissionService.insertBatchPermission(ResourceType.APPLICATION, applicationId,
                        userIds, groupIds, role))
                .thenReturn(true);
    }


    /**
     * 更新应用的权限。
     *
     * @param applicationId 应用ID
     * @param permissionId  权限ID
     * @param role          角色
     * @return 布尔值Mono
     */
    public Mono<Boolean> updatePermission(String applicationId, String permissionId, ResourceRole role) {
        return checkCurrentUserApplicationPermission(applicationId, MANAGE_APPLICATIONS)
                .then(checkApplicationStatus(applicationId, NORMAL))
                .then(resourcePermissionService.getById(permissionId))
                .filter(permission -> StringUtils.equals(permission.getResourceId(), applicationId))
                .switchIfEmpty(deferredError(ILLEGAL_APPLICATION_PERMISSION_ID, "ILLEGAL_APPLICATION_PERMISSION_ID"))
                .then(resourcePermissionService.updateRoleById(permissionId, role));

    }

    /**
     * 删除应用的权限。
     *
     * @param applicationId 应用ID
     * @param permissionId  权限ID
     * @return 布尔值Mono
     */
    public Mono<Boolean> removePermission(String applicationId, String permissionId) {
        return checkCurrentUserApplicationPermission(applicationId, MANAGE_APPLICATIONS)
                .then(checkApplicationStatus(applicationId, NORMAL))
                .then(resourcePermissionService.getById(permissionId))
                .filter(permission -> StringUtils.equals(permission.getResourceId(), applicationId))
                .switchIfEmpty(deferredError(ILLEGAL_APPLICATION_PERMISSION_ID, "ILLEGAL_APPLICATION_PERMISSION_ID"))
                .then(resourcePermissionService.removeById(permissionId));
    }

    /**
     * 获取应用程序权限视图。
     *
     * @param applicationId 应用程序ID
     * @return 包含应用程序权限视图的Mono对象
     */
    public Mono<ApplicationPermissionView> getApplicationPermissions(String applicationId) {

        // 获取应用程序权限列表，并缓存结果
        Mono<List<ResourcePermission>> applicationPermissions = resourcePermissionService.getByApplicationId(applicationId).cache();

        // 获取组权限对的Mono对象
        Mono<List<PermissionItemView>> groupPermissionPairsMono = applicationPermissions
                .flatMap(permissionHelper::getGroupPermissions);

        // 获取用户权限对的Mono对象
        Mono<List<PermissionItemView>> userPermissionPairsMono = applicationPermissions
                .flatMap(permissionHelper::getUserPermissions);

        // 检查当前用户对应用程序的读取权限
        return checkCurrentUserApplicationPermission(applicationId, READ_APPLICATIONS)
                // 查找不使用DSL的应用程序
                .then(applicationService.findByIdWithoutDsl(applicationId))
                // 在执行其他操作之前检查应用程序状态
                .delayUntil(application -> checkApplicationStatus(application, NORMAL))
                .flatMap(application -> {
                    String creatorId = application.getCreatedBy(); // 获取创建者ID
                    String orgId = application.getOrganizationId(); // 获取组织ID

                    // 获取组织信息的Mono对象
                    Mono<Organization> orgMono = organizationService.getById(orgId);

                    // 将组权限对、用户权限对和组织信息合并到一起
                    return Mono.zip(groupPermissionPairsMono, userPermissionPairsMono, orgMono)
                            .map(tuple -> {
                                List<PermissionItemView> groupPermissionPairs = tuple.getT1(); // 获取组权限对列表
                                List<PermissionItemView> userPermissionPairs = tuple.getT2(); // 获取用户权限对列表
                                Organization organization = tuple.getT3(); // 获取组织信息
                                return ApplicationPermissionView.builder()
                                        .groupPermissions(groupPermissionPairs) // 设置组权限对
                                        .userPermissions(userPermissionPairs) // 设置用户权限对
                                        .creatorId(creatorId) // 设置创建者ID
                                        .orgName(organization.getName()) // 设置组织名称
                                        .publicToAll(application.isPublicToAll()) // 设置应用程序是否公开
                                        .build();
                            });
                });
    }


    /**
     * 从模板创建应用程序。
     *
     * @param templateId 模板ID
     * @return 包含应用程序视图的Mono对象
     */
    public Mono<ApplicationView> createFromTemplate(String templateId) {
        // 获取当前访客的组织成员缓存
        return sessionUserService.getVisitorOrgMemberCache()
                // 检查当前组织是否为开发组织
                .delayUntil(orgMember -> orgDevChecker.checkCurrentOrgDev())
                // 检查组织应用程序数量是否达到上限
                .delayUntil(bizThresholdChecker::checkMaxOrgApplicationCount)
                .flatMap(orgMember ->
                        // 从模板创建应用程序
                        templateSolution.createFromTemplate(templateId, orgMember.getOrgId(), orgMember.getUserId())
                                // 构建应用程序视图
                                .map(applicationCreated -> ApplicationView.builder()
                                        .applicationInfoView(buildView(applicationCreated)) // 设置应用程序信息视图
                                        .applicationDSL(applicationCreated.getEditingApplicationDSL()) // 设置应用程序DSL
                                        .build()));
    }


    /**
     * 检查用户对资源的权限，并在没有权限时提供可读的错误信息。
     *
     * @param applicationId 应用程序ID
     * @param action        要执行的资源操作
     * @return 包含资源权限的Mono对象
     */
    @Nonnull
    public Mono<ResourcePermission> checkPermissionWithReadableErrorMsg(String applicationId, ResourceAction action) {
        // 获取当前访客的ID
        return sessionUserService.getVisitorId()
                // 检查用户对资源的权限状态
                .flatMap(visitorId -> resourcePermissionService.checkUserPermissionStatusOnResource(visitorId, applicationId, action))
                .flatMap(permissionStatus -> {
                    // 如果用户没有权限
                    if (!permissionStatus.hasPermission()) {
                        // 检查用户是否因匿名用户而失败
                        if (permissionStatus.failByAnonymousUser()) {
                            return ofError(USER_NOT_SIGNED_IN, "USER_NOT_SIGNED_IN"); // 用户未登录错误
                        }

                        // 检查用户是否因不在组织中而失败
                        if (permissionStatus.failByNotInOrg()) {
                            return ofError(NO_PERMISSION_TO_REQUEST_APP, "INSUFFICIENT_PERMISSION"); // 权限不足错误
                        }

                        // 获取建议的应用程序管理员名称
                        return suggestAppAdminSolution.getSuggestAppAdminNames(applicationId)
                                .flatMap(names -> {
                                    String messageKey = action == EDIT_APPLICATIONS ? "NO_PERMISSION_TO_EDIT" : "NO_PERMISSION_TO_VIEW";
                                    return ofError(NO_PERMISSION_TO_REQUEST_APP, messageKey, names); // 没有权限错误
                                });
                    }
                    // 如果用户有权限，返回权限信息
                    return Mono.just(permissionStatus.getPermission());
                });
    }

    /**
     * 构建应用程序信息视图。
     *
     * @param application 应用程序对象
     * @param role        用户角色
     * @return 包含应用程序信息视图的对象
     */
    private ApplicationInfoView buildView(Application application, String role) {
        return buildView(application, role, null);
    }


    /**
     * 构建应用程序信息视图。
     *
     * @param application 应用程序对象
     * @param role        用户角色
     * @param folderId    文件夹ID（可为空）
     * @return 包含应用程序信息视图的对象
     */
    private ApplicationInfoView buildView(Application application, String role, @Nullable String folderId) {
        return ApplicationInfoView.builder()
                .applicationId(application.getId()) // 设置应用程序ID
                .orgId(application.getOrganizationId()) // 设置组织ID
                .name(application.getName()) // 设置应用程序名称
                .createBy(application.getCreatedBy()) // 设置创建者ID
                .createAt(application.getCreatedAt().toEpochMilli()) // 设置创建时间（以毫秒为单位的时间戳）
                .role(role) // 设置用户角色
                .applicationType(application.getApplicationType()) // 设置应用程序类型
                .applicationStatus(application.getApplicationStatus()) // 设置应用程序状态
                .folderId(folderId) // 设置文件夹ID
                .publicToAll(application.isPublicToAll()) // 设置应用程序是否公开
                .build();
    }

    /**
     * 构建应用程序信息视图。
     *
     * @param application 应用程序对象
     * @return 包含应用程序信息视图的对象
     */
    private ApplicationInfoView buildView(Application application) {
        return buildView(application, ""); // 调用带有角色参数的方法，角色为空字符串
    }

    /**
     * 设置应用程序是否公开。
     *
     * @param applicationId 应用程序ID
     * @param publicToAll   是否公开
     * @return 包含布尔值的Mono对象，表示操作是否成功
     */
    public Mono<Boolean> setApplicationPublicToAll(String applicationId, boolean publicToAll) {
        return checkCurrentUserApplicationPermission(applicationId, ResourceAction.SET_APPLICATIONS_PUBLIC)
                .then(checkApplicationStatus(applicationId, NORMAL))
                .then(applicationService.setApplicationPublicToAll(applicationId, publicToAll));
    }

    /**
     * 清理应用程序DSL中的敏感信息。
     *
     * @param applicationDsl 应用程序DSL
     * @return 清理后的应用程序DSL
     */
    private Map<String, Object> sanitizeDsl(Map<String, Object> applicationDsl) {
        if (applicationDsl.get("queries") instanceof List<?> queries) {
            List<Map<String, Object>> list = queries.stream().map(this::doSanitizeQuery).toList();
            applicationDsl.put("queries", list);
            return applicationDsl;
        }
        return applicationDsl;
    }

    /**
     * 清理查询对象中的敏感信息。
     *
     * @param query 待清理的查询对象
     * @return 清理后的查询对象
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> doSanitizeQuery(Object query) {
        // 如果查询对象不是Map类型，则返回一个空Map
        if (!(query instanceof Map)) {
            return Maps.newHashMap();
        }
        Map<String, Object> queryMap = (Map<String, Object>) query;
        // 获取查询类型
        Object compType = ((Map<?, ?>) query).get("compType");
        // 如果查询类型不是字符串类型，则直接返回原始查询对象
        if (!(compType instanceof String datasourceType)) {
            return queryMap;
        }
        // 如果数据源类型为特定类型，则直接返回原始查询对象
        if (LIBRARY_QUERY_DATASOURCE_TYPE.equalsIgnoreCase(datasourceType) ||
                JS_DATASOURCE_TYPE.equalsIgnoreCase(datasourceType) ||
                VIEW_DATASOURCE_TYPE.equalsIgnoreCase(datasourceType)) {
            return queryMap;
        }
        QueryExecutor<?, Object, ?> queryExecutor;
        try {
            // 获取查询执行器
            queryExecutor = datasourceMetaInfoService.getQueryExecutor(datasourceType);
        } catch (Exception e) {
            // 获取查询执行器失败，直接返回原始查询对象
            return queryMap;
        }
        Object comp = queryMap.get("comp");
        // 如果查询配置不是Map类型，则直接返回原始查询对象
        if (!(comp instanceof Map<?, ?> queryConfig)) {
            return queryMap;
        }
        Map<String, Object> sanitizedQueryConfig;
        try {
            // 清理查询配置
            sanitizedQueryConfig = queryExecutor.sanitizeQueryConfig((Map<String, Object>) queryConfig);
        } catch (Exception e) {
            // 清理查询配置失败，直接返回原始查询对象
            return queryMap;
        }
        queryMap.put("comp", sanitizedQueryConfig); // 将清理后的查询配置放回到Map中
        // 如果查询配置被脱敏，则设置查询类型为"view"
        if (isDesensitizedQueryConfig(sanitizedQueryConfig)) {
            queryMap.put("compType", "view");
        }
        return queryMap;
    }

    /**
     * 检查查询配置是否被脱敏。
     *
     * @param queryConfig 查询配置
     * @return 如果查询配置仅包含"fields"字段，则返回true；否则返回false
     */
    private boolean isDesensitizedQueryConfig(Map<String, Object> queryConfig) {
        return queryConfig.size() == 1 && queryConfig.containsKey("fields");
    }

    /**
     * 检查应用程序的数据源权限。
     *
     * @param application 应用程序对象
     * @return 一个表示检查数据源权限的Mono，成功完成表示权限检查通过，否则抛出异常
     */
    private Mono<Void> checkDatasourcePermissions(Application application) {
        return Mono.defer(() -> {
            // 获取应用程序中所有数据源的ID集合
            Set<String> datasourceIds = SetUtils.emptyIfNull(application.getEditingQueries())
                    .stream()
                    .map(applicationQuery -> applicationQuery.getBaseQuery().getDatasourceId())
                    .filter(StringUtils::isNotBlank)
                    .filter(Datasource::isNotSystemStaticId)
                    .collect(Collectors.toSet());
            // 如果数据源ID集合为空，则直接返回空Mono
            if (CollectionUtils.isEmpty(datasourceIds)) {
                return Mono.empty();
            }

            String organizationId = application.getOrganizationId();
            // 获取访客ID，用于检查权限
            return sessionUserService.getVisitorId()
                    .flatMap(userId -> resourcePermissionService.getMaxMatchingPermission(userId, datasourceIds, USE_DATASOURCES))
                    // 将数据源ID集合与当前组织不存在或不属于当前组织的数据源ID集合进行比较
                    .zipWith(datasourceService.retainNoneExistAndNonCurrentOrgDatasourceIds(datasourceIds, organizationId).collectList())
                    .flatMap(tuple -> {
                        Set<String> hasPermissionDatasourceIds = tuple.getT1().keySet();
                        List<String> noneExistDatasourceIds = tuple.getT2();

                        // 如果用户拥有所有数据源的权限或者数据源不存在或不属于当前组织，则权限检查通过，返回空Mono；否则抛出异常
                        if (Sets.union(hasPermissionDatasourceIds, new HashSet<>(noneExistDatasourceIds)).containsAll(datasourceIds)) {
                            return Mono.empty();
                        }
                        return ExceptionUtils.ofError(BizError.NOT_AUTHORIZED, "APPLICATION_EDIT_ERROR_LACK_OF_DATASOURCE_PERMISSIONS");
                    });
        });
    }
}

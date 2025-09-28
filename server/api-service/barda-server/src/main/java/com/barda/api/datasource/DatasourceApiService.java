package com.barda.api.datasource;

import static com.barda.domain.permission.model.ResourceAction.MANAGE_DATASOURCES;
import static com.barda.domain.permission.model.ResourceAction.READ_APPLICATIONS;
import static com.barda.domain.permission.model.ResourceAction.USE_DATASOURCES;
import static com.barda.sdk.exception.BizError.NOT_AUTHORIZED;
import static com.barda.sdk.util.ExceptionUtils.deferredError;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.api.application.ApplicationApiService;
import com.barda.api.home.SessionUserService;
import com.barda.api.permission.PermissionHelper;
import com.barda.api.permission.view.CommonPermissionView;
import com.barda.api.permission.view.PermissionItemView;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.domain.application.service.ApplicationService;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceStatus;
import com.barda.domain.datasource.repository.DatasourceRepository;
import com.barda.domain.datasource.service.DatasourceConnectionPool;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.datasource.service.JsDatasourceHelper;
import com.barda.domain.organization.model.Organization;
import com.barda.domain.organization.service.OrgMemberService;
import com.barda.domain.organization.service.OrganizationService;
import com.barda.domain.permission.model.ResourcePermission;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.permission.model.ResourceType;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.plugin.client.DatasourcePluginClient;
import com.barda.domain.plugin.client.dto.GetPluginDynamicConfigRequestDTO;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.constants.FieldName;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.exception.ServerException;
import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.models.HasIdAndAuditing;
import com.barda.sdk.models.JsDatasourceConnectionConfig;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 数据源 API 服务类。
 * 该类使用 Spring 的注解来定义服务。
 *
 * @Service 表明该类是一个 Spring 服务。
 */
@Service
public class DatasourceApiService {

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
     * 数据源服务。
     */
    @Autowired
    private DatasourceService datasourceService;

    /**
     * 资源权限服务。
     */
    @Autowired
    private ResourcePermissionService resourcePermissionService;

    /**
     * 权限帮助器。
     */
    @Autowired
    private PermissionHelper permissionHelper;

    /**
     * 组织开发者检查器。
     */
    @Autowired
    private OrgDevChecker orgDevChecker;

    /**
     * 用户服务。
     */
    @Autowired
    private UserService userService;

    /**
     * 数据源连接池。
     */
    @Autowired
    private DatasourceConnectionPool datasourceConnectionPool;

    /**
     * 组织服务。
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * 应用服务。
     */
    @Autowired
    private ApplicationService applicationService;

    /**
     * JS 数据源帮助器。
     */
    @Autowired
    private JsDatasourceHelper jsDatasourceHelper;

    /**
     * 数据源元信息服务。
     */
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    /**
     * 数据源插件客户端。
     */
    @Autowired
    private DatasourcePluginClient datasourcePluginClient;

    /**
     * 数据源存储库。
     */
    @Autowired
    private DatasourceRepository datasourceRepository;

    /**
     * 应用 API 服务。
     */
    @Autowired
    private ApplicationApiService applicationApiService;

    /**
     * 创建数据源。
     *
     * @param datasource 要创建的数据源
     * @return 创建的新数据源
     */
    public Mono<Datasource> create(Datasource datasource) {
        return sessionUserService.getVisitorId()
                .flatMap(userId -> orgMemberService.getOrgMember(datasource.getOrganizationId(), userId))
                .switchIfEmpty(deferredError(NOT_AUTHORIZED, "NOT_AUTHORIZED"))
                .delayUntil(orgMember -> orgDevChecker.checkCurrentOrgDev())
                .flatMap(orgMember -> datasourceService.create(datasource, orgMember.getUserId()))
                .delayUntil(jsDatasourceHelper::processDynamicQueryConfig);
    }

    /**
     * 获取应用的 JS 插件数据源。
     *
     * @param applicationId 应用 ID
     * @return 应用的 JS 插件数据源列表
     */
    public Flux<Datasource> listJsDatasourcePlugins(String applicationId) {
        return applicationService.findById(applicationId)
                .delayUntil(application -> applicationApiService.checkPermissionWithReadableErrorMsg(applicationId, READ_APPLICATIONS))
                .flatMapMany(application -> datasourceService.getByOrgId(application.getOrganizationId()))
                .filter(datasource -> datasource.getDatasourceStatus() == DatasourceStatus.NORMAL)
                .filter(datasource -> datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType()))
                .delayUntil(datasource -> jsDatasourceHelper.processDynamicQueryConfig(datasource))
                .doOnNext(datasource -> datasource.setDetailConfig(null));
    }

    /**
     * 获取应用的数据源。
     *
     * @param appId 应用 ID
     * @return 应用的数据源列表
     */
    public Flux<DatasourceView> listAppDataSources(String appId) {
        return applicationService.findById(appId)
                .flatMapMany(application -> listOrgDataSources(application.getOrganizationId()));
    }

    /**
     * 获取组织的数据源。
     *
     * @param orgId 组织 ID
     * @return 组织的数据源列表
     */
    public Flux<DatasourceView> listOrgDataSources(String orgId) {
        // 获取数据源
        Flux<Datasource> datasourceFlux = datasourceService.getByOrgId(orgId)
                .filter(datasource -> datasource.getDatasourceStatus() == DatasourceStatus.NORMAL)
                .cache();

        // 获取用户-数据源权限
        Mono<Map<String, ResourcePermission>> datasourceId2MaxPermissionMapMono = datasourceFlux
                .map(Datasource::getId)
                .collectList()
                .zipWith(sessionUserService.getVisitorId())
                .flatMap(tuple -> {
                    List<String> allDatasourceIds = tuple.getT1();
                    String visitorId = tuple.getT2();
                    return resourcePermissionService.getMaxMatchingPermission(visitorId, allDatasourceIds, USE_DATASOURCES);
                })
                .cache();

        // 过滤用户-数据源权限
        datasourceFlux = datasourceFlux
                .filterWhen(datasource ->
                        datasourceId2MaxPermissionMapMono.map(map -> {
                            ResourcePermission maxPermission = map.get(datasource.getId());
                            return maxPermission != null && maxPermission.getResourceRole().canDo(USE_DATASOURCES);
                        }))
                .cache();

        // 获取数据源创建者
        Mono<Map<String, User>> userMapMono = datasourceFlux.map(Datasource::getCreatedBy)
                .collectList()
                .flatMap(userIds -> userService.getByIds(userIds))
                .cache();

        // 构建视图
        return datasourceFlux
                .delayUntil(datasourceService::removePasswordTypeKeysFromJsDatasourcePluginConfig)
                .delayUntil(jsDatasourceHelper::processDynamicQueryConfig)
                .flatMap(datasource ->
                        Mono.zip(datasourceId2MaxPermissionMapMono, userMapMono)
                                .map(tuple -> {
                                    Map<String, ResourcePermission> datasourceId2MaxPermissionMap = tuple.getT1();
                                    Map<String, User> userMap = tuple.getT2();
                                    User creator = userMap.get(datasource.getCreatedBy());
                                    ResourcePermission maxPermission = datasourceId2MaxPermissionMap.get(datasource.getId());
                                    boolean manage = maxPermission != null && maxPermission.getResourceRole().canDo(MANAGE_DATASOURCES);
                                    return new DatasourceView(datasource, manage, creator == null ? null : creator.getName());
                                }));
    }

    /**
     * 更新数据源。
     *
     * @param datasourceId 数据源 ID
     * @param updatedDatasource 要更新的数据源
     * @return 更新的新数据源
     */
    public Mono<Datasource> update(String datasourceId, Datasource updatedDatasource) {
        if (datasourceId == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }

        return checkCurrentUserManageDatasourcePermission(datasourceId)
                .then(datasourceService.update(datasourceId, updatedDatasource));
    }

    /**
     * 获取数据源。
     *
     * @param datasourceId 数据源 ID
     * @return 获取的数据源
     */
    public Mono<Datasource> findByIdWithPermission(String datasourceId) {
        return checkCurrentUserManageDatasourcePermission(datasourceId)
                .then(datasourceService.getById(datasourceId));
    }

    /**
     * 测试数据源。
     *
     * @param testDatasource 要测试的数据源
     * @return 测试结果
     */
    public Mono<DatasourceTestResult> testDatasource(Datasource testDatasource) {
        return Mono.defer(() -> {
                    if (testDatasource.getId() != null) {
                        return checkCurrentUserManageDatasourcePermission(testDatasource.getId());
                    }
                    return Mono.empty();
                })
                .then(datasourceService.testDatasource(testDatasource));
    }

    /**
     * 删除数据源。
     *
     * @param datasourceId 数据源 ID
     * @return 删除是否成功
     */
    public Mono<Boolean> delete(String datasourceId) {
        return checkCurrentUserManageDatasourcePermission(datasourceId)
                .then(datasourceService.delete(datasourceId));
    }

    /**
     * 获取数据源的详细信息。
     *
     * @param datasourceId 数据源 ID
     * @return 数据源的详细信息
     */
    public Object info(@Nullable String datasourceId) {
        return datasourceConnectionPool.info(datasourceId);
    }

    /**
     * 获取数据源的插件动态配置。
     *
     * @param getPluginDynamicConfigRequestDTOS 获取插件动态配置的请求列表
     * @return 获取的插件动态配置列表
     */
    public Mono<List<Object>> getPluginDynamicConfig(List<GetPluginDynamicConfigRequestDTO> getPluginDynamicConfigRequestDTOS) {
        if (CollectionUtils.isEmpty(getPluginDynamicConfigRequestDTOS)) {
            return Mono.just(Collections.emptyList());
        }
        Set<String> datasourceIds = getPluginDynamicConfigRequestDTOS.stream()
                .map(GetPluginDynamicConfigRequestDTO::getDataSourceId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(datasourceIds)) {
            return datasourcePluginClient.getPluginDynamicConfig(getPluginDynamicConfigRequestDTOS);
        }
        return datasourceRepository.findAllById(datasourceIds)
                .filter(datasource -> datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())
                        && datasource.getDetailConfig() instanceof JsDatasourceConnectionConfig jsDatasourceConnectionConfig
                        && jsDatasourceConnectionConfig.getExtra() != null)
                .collectMap(HasIdAndAuditing::getId, datasource -> {
                    JsDatasourceConnectionConfig detailConfig = (JsDatasourceConnectionConfig) datasource.getDetailConfig();
                    return detailConfig.getExtra();
                })
                .doOnNext(datasourceId2ExtraMap -> getPluginDynamicConfigRequestDTOS
                        .forEach(getPluginDynamicConfigRequestDTO -> {
                            if (StringUtils.isNotBlank(getPluginDynamicConfigRequestDTO.getDataSourceId())) {
                                Object extra = datasourceId2ExtraMap.get(getPluginDynamicConfigRequestDTO.getDataSourceId());
                                getPluginDynamicConfigRequestDTO.getDataSourceConfig().put("extra", extra);
                            }
                        }))
                .then(datasourcePluginClient.getPluginDynamicConfig(getPluginDynamicConfigRequestDTOS));
    }

    // ================================ PERMISSIONS ================================

    /**
     * 获取数据源的权限。
     *
     * @param datasourceId 数据源 ID
     * @return 获取的数据源的权限视图
     */
    public Mono<CommonPermissionView> getPermissions(String datasourceId) {
        Mono<List<ResourcePermission>> allPermissionListMono = resourcePermissionService.getByDataSourceId(datasourceId).cache();
        Mono<List<PermissionItemView>> groupPermissionListMono = allPermissionListMono.flatMap(permissionHelper::getGroupPermissions);
        Mono<List<PermissionItemView>> userPermissionListMono = allPermissionListMono.flatMap(permissionHelper::getUserPermissions);

        return datasourceService.getById(datasourceId)
                .switchIfEmpty(Mono.error(new ServerException("data source not exist. {}", datasourceId)))
                .delayUntil(__ -> checkCurrentUserManageDatasourcePermission(datasourceId))
                .flatMap(datasource -> Mono.zip(groupPermissionListMono, userPermissionListMono, Mono.just(datasource.getCreatedBy()),
                        organizationService.getById(datasource.getOrganizationId())))
                .map(tuple -> {
                    List<PermissionItemView> groupPermissions = tuple.getT1();
                    List<PermissionItemView> userPermissions = tuple.getT2();
                    String creator = tuple.getT3();
                    Organization organization = tuple.getT4();
                    return CommonPermissionView.builder()
                            .groupPermissions(groupPermissions)
                            .userPermissions(userPermissions)
                            .creatorId(creator)
                            .orgName(organization.getName())
                            .build();
                });
    }

    /**
     * 授予数据源的权限。
     *
     * @param datasourceId 数据源 ID
     * @param userIds 用户 ID 集合
     * @param groupIds 组 ID 集合
     * @param role 角色
     * @return 授予是否成功
     */
    public Mono<Boolean> grantPermission(String datasourceId, @Nullable Set<String> userIds, @Nullable Set<String> groupIds, ResourceRole role) {
        if (CollectionUtils.isEmpty(userIds) && CollectionUtils.isEmpty(groupIds)) {
            return Mono.just(true);
        }

        return checkCurrentUserManageDatasourcePermission(datasourceId)
                .then(checkRole(role))
                .then(datasourceService.getById(datasourceId))
                .switchIfEmpty(deferredError(BizError.DATASOURCE_NOT_FOUND, "DATASOURCE_NOT_FOUND", datasourceId))
                .then(resourcePermissionService.insertBatchPermission(ResourceType.DATASOURCE, datasourceId, userIds, groupIds, role))
                .thenReturn(true);
    }

    /**
     * 更新数据源的权限。
     *
     * @param permissionId 权限 ID
     * @param role 角色
     * @return 更新是否成功
     */
    public Mono<Boolean> updatePermission(String permissionId, ResourceRole role) {
        return checkBeforePermissionDeleteOrUpdate(permissionId)
                .then(checkRole(role))
                .then(resourcePermissionService.updateRoleById(permissionId, role));
    }

    /**
     * 删除数据源的权限。
     *
     * @param permissionId 权限 ID
     * @return 删除是否成功
     */
    public Mono<Boolean> deletePermission(String permissionId) {
        return checkBeforePermissionDeleteOrUpdate(permissionId)
                .then(resourcePermissionService.removeById(permissionId));
    }

    /**
     * 检查角色是否有效。
     *
     * @param role 角色
     * @return 如果角色有效，则返回一个空的Mono；否则返回一个包含错误信息的Mono
     */
    private Mono<Void> checkRole(ResourceRole role) {
        if (MANAGE_DATASOURCES.getRole() == role || USE_DATASOURCES.getRole() == role) {
            return Mono.empty();
        }
        return Mono.error(new ServerException("错误的数据源角色. {}", role));
    }

    /**
     * 在删除或更新权限之前检查权限是否存在且符合要求。
     *
     * @param permissionId 权限ID
     * @return 如果权限存在且符合要求，则返回一个空的Mono；否则返回一个包含错误信息的Mono
     */
    private Mono<Void> checkBeforePermissionDeleteOrUpdate(String permissionId) {
        return resourcePermissionService.getById(permissionId)
                .switchIfEmpty(Mono.error(new ServerException("permission not exist. {}", permissionId)))
                .delayUntil(resourcePermission -> {
                    if (resourcePermission.getResourceType() != ResourceType.DATASOURCE) {
                        return Mono.error(new ServerException("resource type should be datasource. {}", permissionId));
                    }
                    return Mono.empty();
                })
                .flatMap(resourcePermission -> checkCurrentUserManageDatasourcePermission(resourcePermission.getResourceId()));
    }

    /**
     * 检查当前用户是否具有管理数据源的权限。
     *
     * @param datasourceId 数据源ID
     * @return 如果当前用户具有管理数据源的权限，则返回一个空的Mono；否则返回一个包含错误信息的Mono
     */
    private Mono<Void> checkCurrentUserManageDatasourcePermission(String datasourceId) {
        return sessionUserService.getVisitorId()
                .flatMap(visitorId -> resourcePermissionService.checkResourcePermissionWithError(visitorId, datasourceId, MANAGE_DATASOURCES));
    }
}

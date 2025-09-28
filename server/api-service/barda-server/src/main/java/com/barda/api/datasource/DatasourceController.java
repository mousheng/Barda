package com.barda.api.datasource;

import static com.barda.infra.event.EventType.DATA_SOURCE_CREATE;
import static com.barda.infra.event.EventType.DATA_SOURCE_DELETE;
import static com.barda.infra.event.EventType.DATA_SOURCE_PERMISSION_DELETE;
import static com.barda.infra.event.EventType.DATA_SOURCE_PERMISSION_GRANT;
import static com.barda.infra.event.EventType.DATA_SOURCE_PERMISSION_UPDATE;
import static com.barda.infra.event.EventType.DATA_SOURCE_UPDATE;
import static com.barda.sdk.exception.BizError.INVALID_PARAMETER;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static com.barda.sdk.util.LocaleUtils.getLocale;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nullable;
import javax.validation.Valid;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;
import com.barda.api.framework.view.ResponseView;
import com.barda.api.permission.view.CommonPermissionView;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.datasource.service.DatasourceStructureService;
import com.barda.domain.permission.model.ResourceRole;
import com.barda.domain.plugin.client.DatasourcePluginClient;
import com.barda.domain.plugin.client.dto.GetPluginDynamicConfigRequestDTO;
import com.barda.infra.constant.NewUrl;
import com.barda.infra.constant.Url;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.models.DatasourceTestResult;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 该类是数据源控制器，用于处理与数据源相关的HTTP请求。
 * 它使用了Spring MVC的注解来定义RESTful API。
 */
@Slf4j
@RestController
@RequestMapping(value = {Url.DATASOURCE_URL, NewUrl.DATASOURCE_URL})
public class DatasourceController {

    /**
     * 用于处理数据源结构的服务。
     */
    private final DatasourceStructureService datasourceStructureService;

    /**
     * 用于处理数据源API的服务。
     */
    private final DatasourceApiService datasourceApiService;

    /**
     * 用于将请求映射到数据源对象的映射器。
     */
    private final UpsertDatasourceRequestMapper upsertDatasourceRequestMapper;

    /**
     * 用于发布业务事件的发布者。
     */
    private final BusinessEventPublisher businessEventPublisher;

    /**
     * 用于处理数据源的服务。
     */
    private final DatasourceService datasourceService;

    /**
     * 用于与数据源插件进行交互的客户端。
     */
    private final DatasourcePluginClient datasourcePluginClient;

    /**
     * 构造函数，用于注入所需的依赖项。
     */
    @Autowired
    public DatasourceController(
            DatasourceStructureService datasourceStructureService,
            DatasourceApiService datasourceApiService,
            UpsertDatasourceRequestMapper upsertDatasourceRequestMapper,
            BusinessEventPublisher businessEventPublisher,
            DatasourceService datasourceService, DatasourcePluginClient datasourcePluginClient) {
        this.datasourceStructureService = datasourceStructureService;
        this.datasourceApiService = datasourceApiService;
        this.upsertDatasourceRequestMapper = upsertDatasourceRequestMapper;
        this.businessEventPublisher = businessEventPublisher;
        this.datasourceService = datasourceService;
        this.datasourcePluginClient = datasourcePluginClient;
    }

    /**
     * 创建一个新的数据源。
     *
     * @param request 包含创建数据源所需信息的请求。
     * @return 创建的数据源。
     */
    @JsonView(JsonViews.Public.class)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseView<Datasource>> create(@Valid @RequestBody UpsertDatasourceRequest request) {
        return datasourceApiService.create(upsertDatasourceRequestMapper.resolve(request))
                .delayUntil(datasourceService::removePasswordTypeKeysFromJsDatasourcePluginConfig)
                .delayUntil(datasource -> businessEventPublisher.publishDatasourceEvent(datasource, DATA_SOURCE_CREATE))
                .map(ResponseView::success);
    }

    /**
     * 获取指定ID的数据源。
     *
     * @param id 数据源的ID。
     * @return 获取的数据源。
     */
    @JsonView(JsonViews.Public.class)
    @GetMapping("/{id}")
    public Mono<ResponseView<Datasource>> getById(@PathVariable String id) {
        return datasourceApiService.findByIdWithPermission(id)
                .delayUntil(datasourceService::removePasswordTypeKeysFromJsDatasourcePluginConfig)
                .map(ResponseView::success);
    }

    /**
     * 更新指定ID的数据源。
     *
     * @param id       数据源的ID。
     * @param request 包含更新数据源所需信息的请求。
     * @return 更新后的数据源。
     */
    @JsonView(JsonViews.Public.class)
    @PutMapping("/{id}")
    public Mono<ResponseView<Datasource>> update(@PathVariable String id,
            @RequestBody UpsertDatasourceRequest request) {
        Datasource resolvedDatasource = upsertDatasourceRequestMapper.resolve(request);
        return datasourceApiService.update(id, resolvedDatasource)
                .delayUntil(datasourceService::removePasswordTypeKeysFromJsDatasourcePluginConfig)
                .delayUntil(datasource -> businessEventPublisher.publishDatasourceEvent(datasource, DATA_SOURCE_UPDATE))
                .map(ResponseView::success);
    }

    /**
     * 删除指定ID的数据源。
     *
     * @param id 数据源的ID。
     * @return 删除操作是否成功。
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseView<Boolean>> delete(@PathVariable String id) {
        return datasourceApiService.delete(id)
                .delayUntil(result -> {
                    if (BooleanUtils.isTrue(result)) {
                        return businessEventPublisher.publishDatasourceEvent(id, DATA_SOURCE_DELETE);
                    }
                    return Mono.empty();
                })
                .map(ResponseView::success);
    }

    /**
     * 测试数据源的连接。
     *
     * @param request 包含测试数据源所需信息的请求。
     * @return 测试结果。
     */
    @PostMapping("/test")
    public Mono<ResponseView<Boolean>> testDatasource(@RequestBody UpsertDatasourceRequest request) {
        Datasource resolvedDatasource = upsertDatasourceRequestMapper.resolve(request);
        return Mono.deferContextual(ctx -> {
            Locale locale = getLocale(ctx);
            return datasourceApiService.testDatasource(resolvedDatasource)
                    .map(datasourceTestResult -> toResponseView(datasourceTestResult, locale));
        });
    }

    /**
     * 将数据源测试结果转换为响应视图。
     *
     * @param datasourceTestResult 数据源测试结果
     * @param locale               区域信息
     * @return 包含布尔值的响应视图，表示测试结果是否成功
     */
    private ResponseView<Boolean> toResponseView(DatasourceTestResult datasourceTestResult, Locale locale) {
        if (datasourceTestResult.isSuccess()) {
            return ResponseView.success(true);
        }
        return ResponseView.error(500, datasourceTestResult.getInvalidMessage(locale));
    }

    /**
     * 获取数据源的结构信息。
     *
     * @param datasourceId 数据源ID
     * @param ignoreCache  是否忽略缓存，默认为false
     * @return 包含数据源结构信息的响应视图
     */
    @GetMapping("/{datasourceId}/structure")
    public Mono<ResponseView<DatasourceStructure>> getStructure(@PathVariable String datasourceId,
            @RequestParam(required = false, defaultValue = "false") boolean ignoreCache) {
        return datasourceStructureService.getStructure(datasourceId, ignoreCache)
                .map(ResponseView::success);
    }


    /**
     * 获取指定应用ID的JS数据源插件列表。
     *
     * @param applicationId 应用ID。
     * @return JS数据源插件列表。
     */
    @GetMapping("/jsDatasourcePlugins")
    public Mono<ResponseView<List<Datasource>>> listJsDatasourcePlugins(@RequestParam("appId") String applicationId) {
        return datasourceApiService.listJsDatasourcePlugins(applicationId)
                .collectList()
                .map(ResponseView::success);
    }

    /**
     * 获取数据源的动态配置。
     *
     * @param getPluginDynamicConfigRequestDTOS 请求DTO列表。
     * @return 动态配置列表。
     */
    @PostMapping("/getPluginDynamicConfig")
    public Mono<ResponseView<List<Object>>> getPluginDynamicConfig(
            @RequestBody List<GetPluginDynamicConfigRequestDTO> getPluginDynamicConfigRequestDTOS) {
        if (CollectionUtils.isEmpty(getPluginDynamicConfigRequestDTOS)) {
            return Mono.just(ResponseView.success(Collections.emptyList()));
        }
        return datasourceApiService.getPluginDynamicConfig(getPluginDynamicConfigRequestDTOS)
                .map(ResponseView::success);
    }

    /**
     * 获取指定组织ID的数据源列表。
     *
     * @param orgId 组织ID。
     * @return 数据源列表。
     */
    @JsonView(JsonViews.Public.class)
    @GetMapping("/listByOrg")
    public Mono<ResponseView<List<DatasourceView>>> listOrgDataSources(@RequestParam(name = "orgId") String orgId) {
        if (StringUtils.isBlank(orgId)) {
            return ofError(BizError.INVALID_PARAMETER, "ORG_ID_EMPTY");
        }
        return datasourceApiService.listOrgDataSources(orgId)
                .collectList()
                .map(ResponseView::success);
    }

    /**
     * 获取指定应用ID的数据源列表。
     *
     * @param applicationId 应用ID。
     * @return 数据源列表。
     */
    @Deprecated
    @JsonView(JsonViews.Public.class)
    @GetMapping("/listByApp")
    public Mono<ResponseView<List<DatasourceView>>> listAppDataSources(@RequestParam(name = "appId") String applicationId) {
        if (StringUtils.isBlank(applicationId)) {
            return ofError(BizError.INVALID_PARAMETER, "INVALID_APP_ID");
        }
        return datasourceApiService.listAppDataSources(applicationId)
                .collectList()
                .map(ResponseView::success);
    }

    /**
     * 获取指定数据源的权限。
     *
     * @param datasourceId 数据源ID。
     * @return 权限信息。
     */
    @GetMapping("/{datasourceId}/permissions")
    public Mono<ResponseView<CommonPermissionView>> getPermissions(@PathVariable("datasourceId") String datasourceId) {
        return datasourceApiService.getPermissions(datasourceId)
                .map(ResponseView::success);
    }

    /**
     * 授予指定数据源的权限。
     *
     * @param datasourceId 数据源ID。
     * @param request      包含授予权限所需信息的请求。
     * @return 授予操作是否成功。
     */
    @PutMapping("/{datasourceId}/permissions")
    public Mono<ResponseView<Boolean>> grantPermission(@PathVariable String datasourceId,
            @RequestBody BatchAddPermissionRequest request) {
        ResourceRole role = ResourceRole.fromValue(request.role());
        if (role == null) {
            return ofError(INVALID_PARAMETER, "INVALID_PARAMETER", request.role());
        }
        return datasourceApiService.grantPermission(datasourceId, request.userIds(), request.groupIds(), role)
                .delayUntil(result -> {
                    if (BooleanUtils.isTrue(result)) {
                        return businessEventPublisher.publishDatasourcePermissionEvent(datasourceId, request.userIds,
                                request.groupIds(), request.role(), DATA_SOURCE_PERMISSION_GRANT);
                    }
                    return Mono.empty();
                })
                .map(ResponseView::success);
    }

    /**
     * 更新指定权限。
     *
     * @param permissionId 权限ID。
     * @param request      包含更新权限所需信息的请求。
     * @return 更新操作是否成功。
     */
    @PutMapping("/permissions/{permissionId}")
    public Mono<ResponseView<Boolean>> updatePermission(@PathVariable("permissionId") String permissionId,
            @RequestBody UpdatePermissionRequest request) {
        if (request.getResourceRole() == null) {
            return ofError(INVALID_PARAMETER, "INVALID_PARAMETER", request.role());
        }
        return datasourceApiService.updatePermission(permissionId, request.getResourceRole())
                .delayUntil(result -> {
                    if (BooleanUtils.isTrue(result)) {
                        return businessEventPublisher.publishDatasourcePermissionEvent(permissionId, DATA_SOURCE_PERMISSION_UPDATE);
                    }
                    return Mono.empty();
                })
                .map(ResponseView::success);
    }

    /**
     * 删除指定权限。
     *
     * @param permissionId 权限ID。
     * @return 删除操作是否成功。
     */
    @DeleteMapping("/permissions/{permissionId}")
    public Mono<ResponseView<Boolean>> deletePermission(@PathVariable("permissionId") String permissionId) {
        return businessEventPublisher.publishDatasourcePermissionEvent(permissionId, DATA_SOURCE_PERMISSION_DELETE)
                .then(datasourceApiService.deletePermission(permissionId))
                .map(ResponseView::success);
    }

    /**
     * 获取数据源的详细信息。
     *
     * @param datasourceId 数据源ID。
     * @return 数据源的详细信息。
     */
    @GetMapping("/info")
    public Mono<ResponseView<Object>> info(@RequestParam(required = false) String datasourceId) {
        return Mono.just(ResponseView.success(datasourceApiService.info(datasourceId)));
    }

    /**
     * 用于批量添加权限的请求。
     */
    private record BatchAddPermissionRequest(String role, Set<String> userIds, Set<String> groupIds) {
    }

    /**
     * 用于更新权限的请求。
     */
    private record UpdatePermissionRequest(String role) {

        @Nullable
        private ResourceRole getResourceRole() {
            return ResourceRole.fromValue(role());
        }
    }
}

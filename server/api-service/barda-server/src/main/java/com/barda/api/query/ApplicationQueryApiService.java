package com.barda.api.query;

import static com.barda.domain.permission.model.ResourceAction.READ_APPLICATIONS;
import static com.barda.sdk.exception.BizError.DATASOURCE_AND_APP_ORG_NOT_MATCH;
import static com.barda.sdk.exception.BizError.INVALID_PARAMETER;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import com.barda.api.home.SessionUserService;
import com.barda.api.query.view.QueryExecutionRequest;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.domain.application.model.Application;
import com.barda.domain.application.service.ApplicationService;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.query.model.ApplicationQuery;
import com.barda.domain.query.model.BaseQuery;
import com.barda.domain.query.model.LibraryQueryCombineId;
import com.barda.domain.query.model.LibraryQueryRecord;
import com.barda.domain.query.service.LibraryQueryRecordService;
import com.barda.domain.query.service.LibraryQueryService;
import com.barda.domain.query.service.QueryExecutionService;
import com.barda.infra.util.TupleUtils;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.models.Property;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.infra.event.QueryExecutionEvent;
import com.barda.sdk.query.QueryVisitorContext;
import com.barda.sdk.util.ExceptionUtils;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Timed;

/**
 * 应用查询 API 服务类。
 * 该类提供执行应用查询的功能。
 */
@Service
public class ApplicationQueryApiService {

    /**
     * 会话用户服务。
     */
    @Autowired
    private SessionUserService sessionUserService;

    /**
     * 图书馆查询服务。
     */
    @Autowired
    private LibraryQueryService libraryQueryService;

    /**
     * 图书馆查询记录服务。
     */
    @Autowired
    private LibraryQueryRecordService libraryQueryRecordService;

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
     * 数据源服务。
     */
    @Autowired
    private DatasourceService datasourceService;

    /**
     * 查询执行服务。
     */
    @Autowired
    private QueryExecutionService queryExecutionService;

    /**
     * 通用配置。
     */
    @Autowired
    private CommonConfig commonConfig;

    /**
     * 业务事件发布器。
     */
    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    /**
     * 服务器端口。
     */
    @Value("${server.port}")
    private int port;

    /**
     * 执行应用查询。
     *
     * @param exchange 服务器 Web 交换器
     * @param queryExecutionRequest 查询执行请求
     * @return 查询执行结果的 Mono 对象
     */
    public Mono<QueryExecutionResult> executeApplicationQuery(ServerWebExchange exchange, QueryExecutionRequest queryExecutionRequest) {
        if (StringUtils.isBlank(queryExecutionRequest.getQueryId())) {
            return ExceptionUtils.ofError(INVALID_PARAMETER, "INVALID_QUERY_ID");
        }
        String appId = queryExecutionRequest.getApplicationId();
        if (StringUtils.isBlank(appId)) {
            return ExceptionUtils.ofError(INVALID_PARAMETER, "INVALID_APP_ID");
        }
        boolean viewMode = queryExecutionRequest.isViewMode();
        String queryId = queryExecutionRequest.getQueryId();
        Mono<Application> appMono = applicationService.findById(appId).cache();
        Mono<ApplicationQuery> appQueryMono = appMono
                .map(app -> app.getQueryByViewModeAndQueryId(viewMode, queryId))
                .cache();

        Mono<BaseQuery> baseQueryMono = appQueryMono.flatMap(this::getBaseQuery).cache();
        Mono<Datasource> datasourceMono = baseQueryMono.flatMap(query -> datasourceService.getById(query.getDatasourceId())
                        .switchIfEmpty(deferredError(BizError.DATASOURCE_NOT_FOUND, "DATASOURCE_NOT_FOUND", query.getDatasourceId())))
                .cache();
        return sessionUserService.getVisitorId()
                .delayUntil(userId -> checkExecutePermission(userId, queryExecutionRequest.getPath(), appId,
                        queryExecutionRequest.isViewMode()))
                .zipWhen(visitorId -> Mono.zip(appMono, appQueryMono, baseQueryMono, datasourceMono), TupleUtils::merge)
                .flatMap(tuple -> {
                    String userId = tuple.getT1();
                    Application app = tuple.getT2();
                    ApplicationQuery appQuery = tuple.getT3();
                    BaseQuery baseQuery = tuple.getT4();
                    Datasource datasource = tuple.getT5();

                    if (shouldCheckDatasourceOrgMatch(datasource) && !StringUtils.equals(datasource.getOrganizationId(), app.getOrganizationId())) {
                        return ofError(DATASOURCE_AND_APP_ORG_NOT_MATCH, "DATASOURCE_AND_APP_ORG_NOT_MATCH");
                    }

                    MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
                    QueryVisitorContext queryVisitorContext = new QueryVisitorContext(userId, app.getOrganizationId(), port, cookies,
                            getAuthParamsAndHeadersInheritFromLogin(userId, app.getOrganizationId()), commonConfig.getDisallowedHosts());
                    return queryExecutionService.executeQuery(datasource, baseQuery.getQueryConfig(), queryExecutionRequest.paramMap(),
                                    appQuery.getTimeoutStr(), queryVisitorContext
                            )
                            .timed()
                            .doOnNext(timed -> onNextOrError(queryExecutionRequest, queryVisitorContext, appQuery, baseQuery,
                                    app, datasource, timed.elapsed().toMillis(), true))
                            .doOnError(throwable -> onNextOrError(queryExecutionRequest, queryVisitorContext, appQuery, baseQuery,
                                    app, datasource, 0, false))
                            .map(Timed::get);
                });
    }

    /**
     * 判断是否需要检查数据源的组织机构是否匹配。
     *
     * @param datasource 数据源
     * @return true - 需要检查，false - 不需要检查
     */
    private boolean shouldCheckDatasourceOrgMatch(Datasource datasource) {
        return !datasource.isSystemStatic() && !datasource.isLegacyQuickRestApi() && !datasource.isLegacyBardaApi();
    }

    /**
     * 检查执行查询的权限。
     *
     * @param userId 用户 ID
     * @param path 路径
     * @param appId 应用 ID
     * @param viewMode 查看模式
     * @return 权限检查结果的 Mono 对象
     */
    protected Mono<Void> checkExecutePermission(String userId, String[] path, String appId, boolean viewMode) {
        if (viewMode) {
            return checkAppPathAndReturnRootAppId(path, appId, true)
                    .flatMap(rootAppId -> resourcePermissionService.checkResourcePermissionWithError(userId, rootAppId,
                            READ_APPLICATIONS));
        }
        return resourcePermissionService.checkResourcePermissionWithError(userId, appId, READ_APPLICATIONS);
    }

    /**
     * 获取根应用 ID。
     *
     * @param path 路径
     * @param appId 应用 ID
     * @param viewMode 查看模式
     * @return 根应用 ID 的 Mono 对象
     */
    private Mono<String> checkAppPathAndReturnRootAppId(String[] path, String appId, boolean viewMode) {
        String rootAppId = getRootAppIdFromPath(path);
        if (StringUtils.isBlank(rootAppId)) {
            return Mono.just(appId);
        }
        Mono<List<Application>> allDependentModules = applicationService.getAllDependentModulesFromApplicationId(rootAppId, viewMode);
        return allDependentModules
                .map(modules -> modules.stream().map(Application::getId).collect(Collectors.toSet()))
                .flatMap(modules -> {
                    if (!modules.contains(appId)) {
                        return ofError(INVALID_PARAMETER, "INVALID_PARAMETER");
                    }
                    return Mono.just(rootAppId);
                });
    }

    /**
     * 获取根应用 ID。
     *
     * @param path 路径
     * @return 根应用 ID
     */
    @Nullable
    private String getRootAppIdFromPath(String[] path) {
        if (ArrayUtils.isEmpty(path)) {
            return null;
        }
        return path[0];
    }

    /**
     * 获取基础查询。
     *
     * @param applicationQuery 应用查询
     * @return 基础查询的 Mono 对象
     */
    private Mono<BaseQuery> getBaseQuery(ApplicationQuery applicationQuery) {
        if (applicationQuery.isUsingLibraryQuery()) {
            return getBaseQueryFromLibraryQuery(applicationQuery);
        }
        return Mono.just(applicationQuery.getBaseQuery());
    }

    /**
     * 获取基础查询。
     *
     * @param query 应用查询
     * @return 基础查询的 Mono 对象
     */
    private Mono<BaseQuery> getBaseQueryFromLibraryQuery(ApplicationQuery query) {
        LibraryQueryCombineId libraryQueryCombineId = query.getLibraryRecordQueryId();
        if (libraryQueryCombineId.isUsingLiveRecord()) {
            return libraryQueryService.getLiveBaseQueryByLibraryQueryId(libraryQueryCombineId.libraryQueryId());
        }
        return libraryQueryRecordService.getById(libraryQueryCombineId.libraryQueryRecordId())
                .map(LibraryQueryRecord::getQuery);
    }

    /**
     * 获取继承自登录的认证参数和头部。
     *
     * @param userId 用户 ID
     * @param orgId 组织机构 ID
     * @return 认证参数和头部的 Mono 对象
     */
    protected Mono<List<Property>> getAuthParamsAndHeadersInheritFromLogin(String userId, String orgId) {
        return Mono.empty();
    }

    /**
     * 处理查询执行的结果。
     *
     * @param queryExecutionRequest 查询执行请求
     * @param queryVisitorContext 查询访问者上下文
     * @param applicationQuery 应用查询
     * @param baseQuery 基础查询
     * @param application 应用
     * @param datasource 数据源
     * @param executeTime 执行时间
     * @param success 执行是否成功
     */
    protected void onNextOrError(QueryExecutionRequest queryExecutionRequest, QueryVisitorContext queryVisitorContext,
            ApplicationQuery applicationQuery, BaseQuery baseQuery, Application application, Datasource datasource,
            long executeTime, boolean success) {
        // 构建审计日志详情
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("applicationId", application.getId());
        detail.put("applicationName", application.getName());
        detail.put("queryId", applicationQuery.getId());
        detail.put("queryName", applicationQuery.getName());
        detail.put("datasourceId", datasource.getId());
        detail.put("datasourceName", datasource.getName());
        detail.put("datasourceType", datasource.getType());
        detail.put("queryConfig", baseQuery.getQueryConfig());
        detail.put("executeTime", executeTime);
        detail.put("success", success);
        detail.put("viewMode", queryExecutionRequest.isViewMode());

        // 构建并发布查询执行事件
        QueryExecutionEvent event = QueryExecutionEvent.builder()
                .userId(queryVisitorContext.getVisitorId())
                .orgId(queryVisitorContext.getApplicationOrgId())
                .detail(detail)
                .build();

        businessEventPublisher.publishQueryExecutionEvent(event);
    }
}

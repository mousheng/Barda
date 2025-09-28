package com.barda.api.query;

import static com.barda.domain.organization.model.OrgMember.NOT_EXIST;
import static com.barda.sdk.exception.BizError.LIBRARY_QUERY_AND_ORG_NOT_MATCH;
import static com.barda.sdk.util.ExceptionUtils.deferredError;
import static com.barda.sdk.util.ExceptionUtils.ofError;
import static org.apache.commons.lang3.StringUtils.firstNonBlank;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import com.barda.api.home.SessionUserService;
import com.barda.api.query.view.LibraryQueryAggregateView;
import com.barda.api.query.view.LibraryQueryPublishRequest;
import com.barda.api.query.view.LibraryQueryRecordMetaView;
import com.barda.api.query.view.LibraryQueryRequestFromJs;
import com.barda.api.query.view.LibraryQueryView;
import com.barda.api.query.view.QueryExecutionRequest;
import com.barda.api.query.view.UpsertLibraryQueryRequest;
import com.barda.api.usermanagement.OrgDevChecker;
import com.barda.api.util.BusinessEventPublisher;
import com.barda.api.util.ViewBuilder;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.service.DatasourceService;
import com.barda.domain.organization.model.OrgMember;
import com.barda.domain.permission.model.ResourceAction;
import com.barda.domain.permission.service.ResourcePermissionService;
import com.barda.domain.query.model.BaseQuery;
import com.barda.domain.query.model.LibraryQuery;
import com.barda.domain.query.model.LibraryQueryCombineId;
import com.barda.domain.query.model.LibraryQueryRecord;
import com.barda.domain.query.service.LibraryQueryRecordService;
import com.barda.domain.query.service.LibraryQueryService;
import com.barda.domain.query.service.QueryExecutionService;
import com.barda.domain.user.model.User;
import com.barda.domain.user.service.UserService;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.PluginCommonError;
import com.barda.sdk.models.Property;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.query.QueryVisitorContext;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Timed;

@Service
public class LibraryQueryApiService {

    @Autowired
    private LibraryQueryService libraryQueryService;

    @Autowired
    private LibraryQueryRecordService libraryQueryRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrgDevChecker orgDevChecker;

    @Autowired
    private SessionUserService sessionUserService;

    @Autowired
    private QueryExecutionService queryExecutionService;

    @Autowired
    private DatasourceService datasourceService;

    @Autowired
    private BusinessEventPublisher businessEventPublisher;

    @Autowired
    private ResourcePermissionService resourcePermissionService;

    @Autowired
    private CommonConfig commonConfig;

    @Value("${server.port}")
    private int port;

    /**
     * 获取库查询列表。
     *
     * @return 库查询视图列表的 Mono 对象
     */
    public Mono<List<LibraryQueryView>> listLibraryQueries() {
        return orgDevChecker.checkCurrentOrgDev()
                .then(sessionUserService.getVisitorOrgMemberCache())
                .flatMapMany(orgMember -> getByOrgIdWithDatasourcePermissions(orgMember.getOrgId()))
                .collectList()
                .flatMap(libraryQueries -> ViewBuilder.multiBuild(libraryQueries,
                        LibraryQuery::getCreatedBy,
                        userService::getByIds,
                        LibraryQueryView::from));
    }

    /**
     * 获取指定组织 ID 的库查询，并根据数据源权限进行过滤。
     *
     * @param orgId 组织 ID
     * @return 库查询的 Flux 对象
     */
    private Flux<LibraryQuery> getByOrgIdWithDatasourcePermissions(String orgId) {
        Flux<LibraryQuery> libraryQueryFlux = libraryQueryService.getByOrganizationId(orgId)
                .cache();

        Mono<List<String>> datasourceIdListMono = libraryQueryFlux.map(libraryQuery -> libraryQuery.getQuery().getDatasourceId())
                .filter(StringUtils::isNotBlank)
                .collectList()
                .cache();

        Mono<HashSet<String>> datasourceIdSetWithPermissionsOrNoneExists = datasourceIdListMono
                .zipWith(sessionUserService.getVisitorId())
                .flatMapMany(tuple -> {
                    List<String> datasourceIds = tuple.getT1();
                    String userId = tuple.getT2();
                    return resourcePermissionService.filterResourceWithPermission(userId, datasourceIds, ResourceAction.USE_DATASOURCES);
                })
                .concatWith(datasourceIdListMono.flatMapMany(
                        datasourceIds -> datasourceService.retainNoneExistAndNonCurrentOrgDatasourceIds(datasourceIds, orgId)))
                .collectList()
                .map(HashSet::new)
                .cache();

        return libraryQueryFlux
                .filterWhen(libraryQuery -> datasourceIdSetWithPermissionsOrNoneExists.map(
                        set -> set.contains(libraryQuery.getQuery().getDatasourceId())));
    }

    /**
     * 创建库查询。
     *
     * @param libraryQuery 库查询
     * @return 库查询视图的 Mono 对象
     */
    public Mono<LibraryQueryView> create(LibraryQuery libraryQuery) {
        return checkLibraryQueryManagementPermission(libraryQuery)
                .then(libraryQueryService.insert(libraryQuery))
                .zipWhen(lb -> userService.findById(lb.getCreatedBy()))
                .map(tuple -> LibraryQueryView.from(tuple.getT1(), tuple.getT2()));
    }

    /**
     * 更新库查询。
     *
     * @param libraryQueryId 库查询 ID
     * @param upsertLibraryQueryRequest 更新库查询请求
     * @return 布尔值的 Mono 对象，表示是否更新成功
     */
    public Mono<Boolean> update(String libraryQueryId, UpsertLibraryQueryRequest upsertLibraryQueryRequest) {
        LibraryQuery updateLibraryQuery = LibraryQuery.builder()
                .name(upsertLibraryQueryRequest.getName())
                .libraryQueryDSL(upsertLibraryQueryRequest.getLibraryQueryDSL())
                .build();
        return checkLibraryQueryManagementPermission(libraryQueryId)
                .then(libraryQueryService.update(libraryQueryId, updateLibraryQuery));
    }

    /**
     * 删除库查询。
     *
     * @param libraryQueryId 库查询 ID
     * @return 空的 Mono 对象
     */
    public Mono<Void> delete(String libraryQueryId) {
        return checkLibraryQueryManagementPermission(libraryQueryId)
                .then(libraryQueryService.delete(libraryQueryId))
                .then(libraryQueryRecordService.deleteAllLibraryQueryTagByLibraryQueryId(libraryQueryId))
                .then();
    }

    /**
     * 发布库查询。
     *
     * @param libraryQueryId 库查询 ID
     * @param libraryQueryPublishRequest 库查询发布请求
     * @return 库查询记录元视图的 Mono 对象
     */
    public Mono<LibraryQueryRecordMetaView> publish(String libraryQueryId, LibraryQueryPublishRequest libraryQueryPublishRequest) {
        return checkLibraryQueryManagementPermission(libraryQueryId)
                .then(libraryQueryService.getById(libraryQueryId))
                .map(libraryQuery -> LibraryQueryRecord.builder()
                        .tag(libraryQueryPublishRequest.tag())
                        .commitMessage(libraryQueryPublishRequest.commitMessage())
                        .libraryQueryId(libraryQuery.getId())
                        .libraryQueryDSL(libraryQuery.getLibraryQueryDSL())
                        .build())
                .flatMap(libraryQueryRecordService::insert)
                .zipWhen(libraryQueryRecord -> userService.findById(libraryQueryRecord.getCreatedBy()))
                .map(tuple -> LibraryQueryRecordMetaView.from(tuple.getT1(), tuple.getT2()));
    }

    /**
     * 获取库查询下拉列表。
     *
     * @return 库查询聚合视图列表的 Mono 对象
     */
    @SuppressWarnings("ConstantConditions")
    public Mono<List<LibraryQueryAggregateView>> dropDownList() {
        Mono<List<LibraryQuery>> libraryQueryListMono = sessionUserService.getVisitorOrgMemberCache()
                .flatMapMany(orgMember -> getByOrgIdWithDatasourcePermissions(orgMember.getOrgId()))
                .collectList()
                .cache();

        Mono<Map<String, List<LibraryQueryRecord>>> recordMapMono = libraryQueryListMono
                .map(libraryQueryList -> libraryQueryList.stream().map(LibraryQuery::getId).toList())
                .flatMap(libraryQueryRecordService::getByLibraryQueryIdIn);
        Mono<Map<String, User>> userMapMono = libraryQueryListMono
                .map(libraryQueryList -> libraryQueryList.stream().map(LibraryQuery::getCreatedBy).toList())
                .flatMap(userService::getByIds);

        return Mono.zip(libraryQueryListMono, recordMapMono, userMapMono)
                .map(tuple -> {
                    List<LibraryQuery> libraryQueryList = tuple.getT1();
                    Map<String, List<LibraryQueryRecord>> recordMap = tuple.getT2();
                    Map<String, User> userMap = tuple.getT3();
                    return libraryQueryList.stream()
                            .map(libraryQuery -> {
                                if (CollectionUtils.isEmpty(recordMap.get(libraryQuery.getId()))) {
                                    User user = userMap.get(libraryQuery.getCreatedBy());
                                    return LibraryQueryAggregateView.from(libraryQuery, user);
                                }
                                List<LibraryQueryRecord> recordList = recordMap.get(libraryQuery.getId());
                                User user = userMap.get(libraryQuery.getCreatedBy());
                                return LibraryQueryAggregateView.from(libraryQuery, user, recordList);
                            }).toList();
                });
    }

    /**
     * 检查库查询管理权限。
     *
     * @param libraryQuery 库查询
     * @return 空的 Mono 对象
     */
    private Mono<Void> checkLibraryQueryManagementPermission(LibraryQuery libraryQuery) {
        return orgDevChecker.checkCurrentOrgDev()
                .then(sessionUserService.getVisitorOrgMemberCache())
                .flatMap(orgMember -> {
                    if (!orgMember.getOrgId().equals(libraryQuery.getOrganizationId())) {
                        return ofError(LIBRARY_QUERY_AND_ORG_NOT_MATCH, "LIBRARY_QUERY_AND_ORG_NOT_MATCH");
                    }
                    return Mono.empty();
                });
    }

    /**
     * 检查库查询管理权限。
     *
     * @param libraryId 库查询 ID
     * @return 空的 Mono 对象
     */
    Mono<Void> checkLibraryQueryManagementPermission(String libraryId) {
        return orgDevChecker.checkCurrentOrgDev()
                .then(sessionUserService.getVisitorOrgMemberCache())
                .zipWith(libraryQueryService.getById(libraryId))
                .flatMap(tuple2 -> {
                    OrgMember orgMember = tuple2.getT1();
                    LibraryQuery libraryQuery = tuple2.getT2();
                    if (!orgMember.getOrgId().equals(libraryQuery.getOrganizationId())) {
                        return ofError(LIBRARY_QUERY_AND_ORG_NOT_MATCH, "LIBRARY_QUERY_AND_ORG_NOT_MATCH");
                    }
                    return Mono.empty();
                });
    }

    /**
     * 检查库查询视图权限。
     *
     * @param libraryId 库查询 ID
     * @return 空的 Mono 对象
     */
    Mono<Void> checkLibraryQueryViewPermission(String libraryId) {
        return sessionUserService.getVisitorOrgMemberCache()
                .zipWith(libraryQueryService.getById(libraryId))
                .flatMap(tuple2 -> {
                    OrgMember orgMember = tuple2.getT1();
                    LibraryQuery libraryQuery = tuple2.getT2();
                    if (!orgMember.getOrgId().equals(libraryQuery.getOrganizationId())) {
                        return ofError(LIBRARY_QUERY_AND_ORG_NOT_MATCH, "LIBRARY_QUERY_AND_ORG_NOT_MATCH");
                    }
                    return Mono.empty();
                });
    }

    /**
     * 从 JavaScript 执行库查询。
     *
     * @param exchange 服务器 Web 交换
     * @param request 库查询请求
     * @return 查询执行结果的 Mono 对象
     */
    public Mono<QueryExecutionResult> executeLibraryQueryFromJs(ServerWebExchange exchange, LibraryQueryRequestFromJs request) {

        Mono<BaseQuery> baseQueryMono = getQueryBaseFromQueryName(request.getLibraryQueryName(), request.getLibraryQueryRecordId()).cache();

        Mono<Datasource> datasourceMono = baseQueryMono.flatMap(query -> datasourceService.getById(query.getDatasourceId())
                        .switchIfEmpty(deferredError(BizError.DATASOURCE_NOT_FOUND, "DATASOURCE_NOT_FOUND", query.getDatasourceId())))
                .cache();

        Mono<OrgMember> visitorOrgMemberCache = sessionUserService.getVisitorOrgMemberCache()
                .onErrorReturn(NOT_EXIST);
        return Mono.zip(visitorOrgMemberCache, baseQueryMono, datasourceMono)
                .flatMap(tuple -> {
                    OrgMember orgMember = tuple.getT1();
                    String orgId = orgMember.getOrgId();
                    String userId = orgMember.getUserId();
                    BaseQuery baseQuery = tuple.getT2();
                    Datasource datasource = tuple.getT3();
                    Mono<List<Property>> paramsAndHeadersInheritFromLogin = orgMember.isInvalid()
                                                                            ? Mono.empty() : getParamsAndHeadersInheritFromLogin(userId, orgId);

                    QueryVisitorContext queryVisitorContext = new QueryVisitorContext(userId, orgId, port,
                            exchange.getRequest().getCookies(),
                            paramsAndHeadersInheritFromLogin,
                            commonConfig.getDisallowedHosts());

                    Map<String, Object> queryConfig = baseQuery.getQueryConfig();
                    String timeoutStr = firstNonBlank(baseQuery.getTimeoutStr(), "5s");

                    return queryExecutionService.executeQuery(datasource, queryConfig, request.paramMap(), timeoutStr,
                                    queryVisitorContext)
                            .onErrorResume(throwable -> Mono.just(QueryExecutionResult.error(PluginCommonError.QUERY_EXECUTION_ERROR,
                                    "QUERY_EXECUTION_ERROR", throwable.getMessage())));
                });
    }

    /**
     * 根据库查询名称和库查询记录ID获取查询基础对象。
     *
     * @param libraryQueryName     库查询名称。
     * @param libraryQueryRecordId 库查询记录ID。
     * @return 包含查询基础对象的Mono。
     */
    private Mono<BaseQuery> getQueryBaseFromQueryName(String libraryQueryName, String libraryQueryRecordId) {
        return libraryQueryService.getByName(libraryQueryName)
                .map(libraryQuery -> new LibraryQueryCombineId(libraryQuery.getId(), libraryQueryRecordId))
                .flatMap(this::getBaseQuery);
    }

    /**
     * 执行库查询。
     *
     * @param exchange 服务器 Web 交换
     * @param queryExecutionRequest 查询执行请求
     * @return 查询执行结果的 Mono 对象
     */
    public Mono<QueryExecutionResult> executeLibraryQuery(ServerWebExchange exchange, QueryExecutionRequest queryExecutionRequest) {

        MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
        Mono<BaseQuery> baseQueryMono = libraryQueryService.getEditingBaseQueryByLibraryQueryId(
                queryExecutionRequest.getLibraryQueryCombineId().libraryQueryId()).cache();
        Mono<Datasource> datasourceMono = baseQueryMono.flatMap(query -> datasourceService.getById(query.getDatasourceId())
                .switchIfEmpty(deferredError(BizError.DATASOURCE_NOT_FOUND, "DATASOURCE_NOT_FOUND", query.getDatasourceId()))).cache();

        return orgDevChecker.checkCurrentOrgDev()
                .then(Mono.zip(sessionUserService.getVisitorOrgMemberCache(),
                        baseQueryMono, datasourceMono))
                .flatMap(tuple -> {
                    OrgMember orgMember = tuple.getT1();
                    String orgId = orgMember.getOrgId();
                    String userId = orgMember.getUserId();
                    BaseQuery baseQuery = tuple.getT2();
                    Datasource datasource = tuple.getT3();
                    Mono<List<Property>> paramsAndHeadersInheritFromLogin =
                            getParamsAndHeadersInheritFromLogin(userId, orgId);
                    QueryVisitorContext queryVisitorContext = new QueryVisitorContext(userId, orgId, port, cookies, paramsAndHeadersInheritFromLogin,
                            commonConfig.getDisallowedHosts());
                    Map<String, Object> queryConfig = baseQuery.getQueryConfig();
                    String timeoutStr = baseQuery.getTimeoutStr();
                    return queryExecutionService.executeQuery(datasource, queryConfig, queryExecutionRequest.paramMap(), timeoutStr,
                                    queryVisitorContext
                            )
                            .timed()
                            .doOnNext(timed -> onNextOrError(queryExecutionRequest, queryVisitorContext, baseQuery, datasource,
                                    timed.elapsed().toMillis(), true))
                            .doOnError(throwable -> onNextOrError(queryExecutionRequest, queryVisitorContext, baseQuery, datasource, 0, false))
                            .map(Timed::get);
                });
    }

    /**
     * 获取基础查询。
     *
     * @param libraryQueryCombineId 库查询组合 ID
     * @return 基础查询的 Mono 对象
     */
    private Mono<BaseQuery> getBaseQuery(LibraryQueryCombineId libraryQueryCombineId) {
        if (libraryQueryCombineId.isUsingEditingRecord()) {
            return libraryQueryService.getById(libraryQueryCombineId.libraryQueryId())
                    .map(LibraryQuery::getQuery);
        }
        if (libraryQueryCombineId.isUsingLiveRecord()) {
            return libraryQueryService.getLiveBaseQueryByLibraryQueryId(libraryQueryCombineId.libraryQueryId());
        }
        return libraryQueryRecordService.getById(libraryQueryCombineId.libraryQueryRecordId())
                .map(LibraryQueryRecord::getQuery);
    }

    /**
     * 获取从登录继承来的参数和头部。
     *
     * @param userId 用户 ID
     * @param orgId 组织 ID
     * @return 参数和头部列表的 Mono 对象
     */
    protected Mono<List<Property>> getParamsAndHeadersInheritFromLogin(String userId, String orgId) {
        return Mono.empty();
    }

    /**
     * 处理查询执行的结果。
     *
     * @param queryExecutionRequest 查询执行请求
     * @param queryVisitorContext 查询访问者上下文
     * @param baseQuery 基础查询
     * @param datasource 数据源
     * @param executeTime 执行时间（毫秒）
     * @param success 是否执行成功
     */
    protected void onNextOrError(QueryExecutionRequest queryExecutionRequest, QueryVisitorContext queryVisitorContext, BaseQuery baseQuery,
            Datasource datasource, long executeTime, boolean success) {
        // do nothing
    }
}

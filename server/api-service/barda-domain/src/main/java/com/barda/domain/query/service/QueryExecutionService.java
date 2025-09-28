package com.barda.domain.query.service;

import static com.barda.sdk.exception.BizError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_TIMEOUT;
import static com.barda.sdk.util.ExceptionUtils.ofException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceConnectionHolder;
import com.barda.domain.datasource.service.DatasourceConnectionPool;
import com.barda.domain.plugin.client.DatasourcePluginClient;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.domain.query.util.QueryTimeoutUtils;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.query.QueryExecutionContext;
import com.barda.sdk.query.QueryVisitorContext;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 查询执行服务类。
 * 它提供对查询的执行操作。
 */
@Slf4j
@Service
public class QueryExecutionService {

    @Autowired
    private DatasourceConnectionPool datasourceConnectionPool;

    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    @Autowired
    private DatasourcePluginClient datasourcePluginClient;

    /**
     * 执行查询。
     *
     * @param datasource 数据源
     * @param queryConfig 查询配置
     * @param requestParams 请求参数
     * @param timeoutStr 超时字符串
     * @param queryVisitorContext 查询访问器上下文
     * @return 查询执行结果
     */
    public Mono<QueryExecutionResult> executeQuery(Datasource datasource, Map<String, Object> queryConfig, Map<String, Object> requestParams,
            String timeoutStr, QueryVisitorContext queryVisitorContext) {

        int timeoutMs = QueryTimeoutUtils.parseQueryTimeoutMs(timeoutStr, requestParams);

        return Mono.defer(() -> {
                    if (datasourceMetaInfoService.isJsDatasourcePlugin(datasource.getType())) {
                        return executeByNodeJs(datasource, queryConfig, requestParams);
                    }
                    return executeLocally(datasource, queryConfig, requestParams, queryVisitorContext);
                })
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorMap(TimeoutException.class, e -> new PluginException(QUERY_EXECUTION_TIMEOUT, "PLUGIN_EXECUTION_TIMEOUT", timeoutMs))
                .onErrorResume(PluginException.class, pluginException -> Mono.just(QueryExecutionResult.error(pluginException)))
                .onErrorMap(exception -> {
                    if (exception instanceof BizException) {
                        return exception;
                    }
                    log.error("查询执行时发生异常", exception);
                    return ofException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", exception.getMessage());
                });
    }

    /**
     * 本地执行查询。
     *
     * @param datasource 数据源
     * @param queryConfig 查询配置
     * @param requestParams 请求参数
     * @param queryVisitorContext 查询访问器上下文
     * @return 查询执行结果
     */
    private Mono<QueryExecutionResult> executeLocally(Datasource datasource, Map<String, Object> queryConfig, Map<String, Object> requestParams,
            QueryVisitorContext queryVisitorContext) {
        var queryExecutor = datasourceMetaInfoService.getQueryExecutor(datasource.getType());

        return queryExecutor.buildQueryExecutionContextMono(datasource.getDetailConfig(), queryConfig, requestParams, queryVisitorContext)
                .zipWhen(context -> datasourceConnectionPool.getOrCreateConnection(datasource))
                .flatMap(tuple -> {
                    QueryExecutionContext queryExecutionRequest = tuple.getT1();
                    DatasourceConnectionHolder connectionHolder = tuple.getT2();
                    return queryExecutor.doExecuteQuery(connectionHolder.connection(), queryExecutionRequest)
                            .doOnError(connectionHolder::onQueryError);
                });
    }

    /**
     * 通过Node.js执行查询。
     *
     * @param datasource 数据源
     * @param queryConfig 查询配置
     * @param requestParams 请求参数
     * @return 查询执行结果
     */
    private Mono<QueryExecutionResult> executeByNodeJs(Datasource datasource, Map<String, Object> queryConfig, Map<String, Object> requestParams) {
        List<Map<String, Object>> context = requestParams.entrySet()
                .stream()
                .map(entry -> Map.of("key", entry.getKey(), "value", entry.getValue()))
                .collect(Collectors.toList());
        return datasourcePluginClient.executeQuery(datasource.getType(), queryConfig, context, datasource.getDetailConfig());
    }
}

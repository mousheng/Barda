package com.barda.plugin.es;

import static com.barda.plugin.es.EsError.ES_EXECUTION_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.pf4j.Extension;

import com.google.common.base.Joiner;
import com.barda.plugin.es.model.EsConnection;
import com.barda.plugin.es.model.EsDatasourceConfig;
import com.barda.plugin.es.model.EsQueryConfig;
import com.barda.plugin.es.model.EsQueryExecutionContext;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.plugin.common.QueryExecutor;
import com.barda.sdk.query.QueryVisitorContext;
import com.barda.sdk.util.JsonUtils;
import com.barda.sdk.util.MustacheHelper;
import com.barda.sdk.util.Preconditions;

import reactor.core.publisher.Mono;

/**
 * Elasticsearch查询执行器。
 *
 * <p>
 * EsQueryExecutor实现了对Elasticsearch的查询操作。
 * 它通过组合EsDatasourceConfig、EsConnection和EsQueryExecutionContext来执行查询。
 * </p>
 */
@Extension
public class EsQueryExecutor implements QueryExecutor<EsDatasourceConfig, EsConnection, EsQueryExecutionContext> {

    private static final Joiner JOINER = Joiner.on("/").skipNulls();

    /**
     * 从datasourceConnectionConfig构建查询执行上下文的Mono。
     *
     * @param connectionConfig    数据源连接配置
     * @param queryConfig         查询配置
     * @param requestParams       请求参数
     * @param queryVisitorContext 查询访问器上下文
     * @return 查询执行上下文的Mono
     */
    @Override
    public Mono<EsQueryExecutionContext> doBuildQueryExecutionContextMono(EsDatasourceConfig connectionConfig, Map<String, Object> queryConfig, Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {
        EsQueryConfig esQueryConfig = JsonUtils.fromJson(JsonUtils.toJson(queryConfig), EsQueryConfig.class);

        // 前置条件
        Preconditions.check(Objects.nonNull(esQueryConfig), QUERY_ARGUMENT_ERROR, "INVALID_ES_QUERY_CONFIG");

        // 渲染
        String prefix = StringUtils.isBlank(esQueryConfig.getPrefix()) ? "" : MustacheHelper.renderMustacheString(esQueryConfig.getPrefix(),
                requestParams);
        String suffix = StringUtils.isBlank(esQueryConfig.getSuffix()) ? "" : MustacheHelper.renderMustacheString(esQueryConfig.getSuffix(),
                requestParams);
        String path = StringUtils.isBlank(esQueryConfig.getPath()) ? "" : MustacheHelper.renderMustacheString(esQueryConfig.getPath(),
                requestParams);
        String dsl = StringUtils.isBlank(esQueryConfig.getDsl()) ? "" : MustacheHelper.renderMustacheJsonString(esQueryConfig.getDsl(),
                requestParams);

        // 去除多余的"/"
        String wholePath = prefix.trim() + "/" + path.trim() + "/" + suffix.trim();
        List<String> splits = Stream.of(wholePath.split("/")).filter(StringUtils::isNotBlank).toList();
        wholePath = JOINER.join(splits);

        return Mono.just(EsQueryExecutionContext.builder()
                .httpMethod(esQueryConfig.getHttpMethod())
                .path(wholePath)
                .dsl(dsl)
                .build());
    }

    /**
     * 非阻塞执行查询。
     *
     * @param esConnection Es连接
     * @param context 查询执行上下文
     * @return 查询执行结果Mono
     */
    @Override
    public Mono<QueryExecutionResult> executeQuery(EsConnection esConnection, EsQueryExecutionContext context) {
        // 构建请求
        Request request = new Request(context.getHttpMethod().name(), "/" + context.getPath());
        if (StringUtils.isNotBlank(context.getDsl())) {
            request.setJsonEntity(context.getDsl());
        }
        return esConnection.reactorRestClientAdaptor()
                .request(request)
                .map(response -> {
                    try {
                        Map<String, Object> map = JsonUtils.fromJsonMap(EntityUtils.toString(response.getEntity()));
                        return QueryExecutionResult.success(map);
                    } catch (IOException e) {
                        return QueryExecutionResult.error(ES_EXECUTION_ERROR, "ES_EXECUTION_ERROR", e.getMessage());
                    }
                })
                .onErrorResume(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        return Mono.just(QueryExecutionResult.error(QUERY_EXECUTION_ERROR, "EXECUTION_TIMEOUT"));
                    }
                    return Mono.just(QueryExecutionResult.error(ES_EXECUTION_ERROR, "ES_QUERY_ERROR", throwable.getMessage()));
                });
    }
}

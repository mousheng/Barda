package com.barda.sdk.plugin.common;

import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.util.ExceptionUtils.ofPluginError;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;
import static com.barda.sdk.util.ExceptionUtils.propagateError;

import java.util.Map;

import org.pf4j.ExtensionPoint;

import com.barda.sdk.exception.BizException;
import com.barda.sdk.exception.PluginCommonError;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.query.QueryExecutionContext;
import com.barda.sdk.query.QueryVisitorContext;
import com.barda.sdk.util.ExceptionUtils;

import reactor.core.publisher.Mono;

/**
 * QueryExecutor接口定义了查询执行器的通用行为。
 * @param <ConnectionConfig> 数据源连接配置类型
 * @param <Connection> 数据库连接类型
 * @param <QueryContext> 查询上下文类型
 */
public interface QueryExecutor<ConnectionConfig extends DatasourceConnectionConfig, Connection, QueryContext extends QueryExecutionContext>
        extends ExtensionPoint {


    /**
     * 构建查询执行上下文的Mono。该方法应当用于构建查询执行上下文。
     *
     * @param datasourceConnectionConfig 数据源连接配置
     * @param queryConfig               查询配置
     * @param requestParams             请求参数
     * @param queryVisitorContext       查询访问器上下文
     * @return 查询执行上下文的Mono
     */
    @SuppressWarnings("unchecked")
    default Mono<QueryContext> buildQueryExecutionContextMono(DatasourceConnectionConfig datasourceConnectionConfig,
            Map<String, Object> queryConfig,
            Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {
        ConnectionConfig connectionConfig;
        try {
            connectionConfig = (ConnectionConfig) datasourceConnectionConfig;
        } catch (ClassCastException e) {
            return propagateError(PluginCommonError.INVALID_QUERY_SETTINGS, "INVALID_QUERY_SETTINGS", e);
        }

        return doBuildQueryExecutionContextMono(connectionConfig, queryConfig, requestParams, queryVisitorContext);
    }

    /**
     * 从datasourceConnectionConfig构建查询执行上下文的Mono。
     *
     * @param connectionConfig       数据源连接配置
     * @param queryConfig            查询配置
     * @param requestParams          请求参数
     * @param queryVisitorContext    查询访问器上下文
     * @return 查询执行上下文的Mono
     */
    @SuppressWarnings("unchecked")
    default Mono<QueryContext> doBuildQueryExecutionContextMono(ConnectionConfig connectionConfig,
            Map<String, Object> queryConfig,
            Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {
        return buildQueryExecutionContextMono(connectionConfig, queryConfig, requestParams, queryVisitorContext)
                .onErrorMap(e -> ExceptionUtils.wrapException(PluginCommonError.INVALID_QUERY_SETTINGS, "QUERY_ARGUMENT_ERROR", e));
    }

    /**
     * 执行查询。
     *
     * @param connection             数据库连接
     * @param queryExecutionContext 查询执行上下文
     * @return 查询执行结果的Mono
     */
    @SuppressWarnings("unchecked")
    default Mono<QueryExecutionResult> doExecuteQuery(Connection connection, QueryExecutionContext queryExecutionContext) {
        QueryContext context;
        try {
            context = (QueryContext) queryExecutionContext;
        } catch (ClassCastException e) {
            return ofPluginError(PluginCommonError.INVALID_QUERY_SETTINGS, "INVALID_QUERY_SETTINGS", e.getMessage());
        }

        return executeQuery(connection, context)
                .onErrorMap(e -> {
                    if (e instanceof PluginException || e instanceof BizException) {
                        return e;
                    }
                    return new PluginException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e.getMessage());
                });
    }

    /**
     * 获取数据源结构的Mono。
     *
     * @param connection             数据库连接
     * @param datasourceConnectionConfig 数据源连接配置
     * @return 数据源结构的Mono
     */
    @SuppressWarnings("unchecked")
    default Mono<DatasourceStructure> doGetStructure(Connection connection, DatasourceConnectionConfig datasourceConnectionConfig) {
        ConnectionConfig connectionConfig;
        try {
            connectionConfig = (ConnectionConfig) datasourceConnectionConfig;
        } catch (ClassCastException e) {
            throw ofPluginException(PluginCommonError.INVALID_QUERY_SETTINGS, "DATASOURCE_GET_STRUCTURE_ERROR", e.getMessage());
        }
        return getStructure(connection, connectionConfig)
                .onErrorMap(e -> {
                    if (e instanceof PluginException) {
                        return e;
                    }
                    return new PluginException(QUERY_EXECUTION_ERROR, "DATASOURCE_GET_STRUCTURE_ERROR", e.getMessage());
                });
    }

    /**
     * 获取数据源结构。该方法应当由SQL-like数据源支持。
     *
     * @param connection             数据库连接
     * @param connectionConfig       数据源连接配置
     * @return 数据源结构的Mono
     */
    default Mono<DatasourceStructure> getStructure(Connection connection, ConnectionConfig connectionConfig) {
        return Mono.empty();
    }

    /**
     * 对查询配置进行清理和修复。
     *
     * @param queryConfig 查询配置
     * @return 清理和修复后的查询配置
     */
    default Map<String, Object> sanitizeQueryConfig(Map<String, Object> queryConfig) {
        return queryConfig;
    }


    Mono<QueryExecutionResult> executeQuery(Connection connection, QueryContext queryExecutionContext);

}

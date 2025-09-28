package com.barda.plugin.clickhouse;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.barda.sdk.exception.PluginCommonError.CONNECTION_ERROR;
import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_GET_STRUCTURE_ERROR;
import static com.barda.sdk.exception.PluginCommonError.PREPARED_STATEMENT_BIND_PARAMETERS_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.getIdenticalColumns;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.querySharedScheduler;
import static com.barda.sdk.plugin.common.sql.ResultSetParser.parseColumns;
import static com.barda.sdk.plugin.common.sql.ResultSetParser.parseRows;
import static com.barda.sdk.util.JsonUtils.toJson;
import static com.barda.sdk.util.MustacheHelper.doPrepareStatement;
import static com.barda.sdk.util.MustacheHelper.extractMustacheKeysInOrder;
import static com.barda.sdk.util.MustacheHelper.renderMustacheString;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import com.barda.plugin.clickhouse.model.ClickHouseDatasourceConfig;
import com.barda.plugin.clickhouse.model.ClickHouseQueryConfig;
import com.barda.plugin.clickhouse.utils.ClickHouseStructureParser;
import com.barda.sdk.config.dynamic.ConfigCenter;
import com.barda.sdk.exception.InvalidHikariDatasourceException;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.models.DatasourceStructure.Table;
import com.barda.sdk.models.LocaleMessage;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.plugin.common.QueryExecutor;
import com.barda.sdk.plugin.common.SqlQueryUtils;
import com.barda.sdk.plugin.common.sql.SqlBasedQueryExecutionContext;
import com.barda.sdk.query.QueryVisitorContext;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * ClickHouse 查询执行器。
 * 实现了 QueryExecutor 接口，用于执行 ClickHouse 数据库的 SQL 查询。
 */
@Slf4j
@Extension
public class ClickHouseQueryExecutor implements QueryExecutor<ClickHouseDatasourceConfig, HikariDataSource, SqlBasedQueryExecutionContext> {

    private final Supplier<Duration> getStructureTimeout;

    /**
     * 构造函数。
     *
     * @param configCenter 配置中心
     */
    public ClickHouseQueryExecutor(ConfigCenter configCenter) {
        this.getStructureTimeout = configCenter.clickHousePlugin().ofInteger("getStructureTimeoutMillis", 8000)
                .then(Duration::ofMillis);
    }

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
    public Mono<SqlBasedQueryExecutionContext> doBuildQueryExecutionContextMono(ClickHouseDatasourceConfig connectionConfig, Map<String, Object> queryConfig, Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {

        var clickHouseQueryConfig = ClickHouseQueryConfig.from(queryConfig);

        String query = SqlQueryUtils.removeQueryComments(clickHouseQueryConfig.getSql().trim());
        if (StringUtils.isBlank(query)) {
            throw new PluginException(QUERY_ARGUMENT_ERROR, "SQL_EMPTY");
        }

        if (!connectionConfig.isEnableTurnOffPreparedStatement() && clickHouseQueryConfig.isDisablePreparedStatement()) {
            throw new PluginException(QUERY_ARGUMENT_ERROR, "CLICKHOUSE_PS_ERROR");
        }

        return Mono.just(SqlBasedQueryExecutionContext.builder()
                .query(query)
                .requestParams(requestParams)
                .disablePreparedStatement(connectionConfig.isEnableTurnOffPreparedStatement() &&
                        clickHouseQueryConfig.isDisablePreparedStatement())
                .build());
    }

    /**
     * 执行查询。
     *
     * @param hikariDataSource      Hikari数据源
     * @param queryExecutionContext SQL查询上下文
     * @return 查询执行结果的Mono对象
     */
    @Override
    public Mono<QueryExecutionResult> executeQuery(HikariDataSource hikariDataSource, SqlBasedQueryExecutionContext queryExecutionContext) {

        String query = queryExecutionContext.getQuery();
        Map<String, Object> requestParams = queryExecutionContext.getRequestParams();
        boolean preparedStatement = !queryExecutionContext.isDisablePreparedStatement();

        return Mono.fromSupplier(() -> executeQuery0(hikariDataSource, query, requestParams, preparedStatement))
                .onErrorMap(e -> {
                    if (e instanceof PluginException) {
                        return e;
                    }
                    return new PluginException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e.getMessage());
                })
                .subscribeOn(querySharedScheduler());
    }

    /**
     * 获取ClickHouse数据结构。
     *
     * @param hikariDataSource Hikari数据源
     * @param connectionConfig ClickHouse数据源配置
     * @return 数据源结构的Mono对象
     */
    @Override
    public Mono<DatasourceStructure> getStructure(HikariDataSource hikariDataSource,
                                                  ClickHouseDatasourceConfig connectionConfig) {

        return Mono.fromCallable(() -> {
                    Connection connection = getConnection(hikariDataSource);

                    Map<String, Table> tablesByName = new LinkedHashMap<>();
                    try (Statement statement = connection.createStatement()) {
                        ClickHouseStructureParser.parseTableAndColumns(tablesByName, statement);
                    } catch (SQLException throwable) {
                        throw new PluginException(DATASOURCE_GET_STRUCTURE_ERROR, "DATASOURCE_GET_STRUCTURE_ERROR",
                                throwable.getMessage());
                    } finally {
                        releaseResources(connection);
                    }

                    DatasourceStructure structure = new DatasourceStructure(new ArrayList<>(tablesByName.values()));
                    for (Table table : structure.getTables()) {
                        table.getKeys().sort(Comparator.naturalOrder());
                    }
                    return structure;
                })
                .timeout(getStructureTimeout.get())
                .subscribeOn(querySharedScheduler());
    }

    /**
     * 执行查询。
     *
     * @param hikariDataSource    Hikari数据源
     * @param query               SQL查询语句
     * @param requestParams       请求参数
     * @param isPreparedStatement 是否使用预处理语句
     * @return 查询执行结果
     */
    private QueryExecutionResult executeQuery0(HikariDataSource hikariDataSource, String query,
                                               Map<String, Object> requestParams,
                                               boolean isPreparedStatement) {

        List<String> mustacheKeysInOrder = extractMustacheKeysInOrder(query);

        Statement statement = null;
        ResultSet resultSet = null;
        PreparedStatement preparedQuery = null;
        boolean isResultSet;

        Connection connection = getConnection(hikariDataSource);
        try {
            if (isPreparedStatement) {
                var preparedSql = doPrepareStatement(query, mustacheKeysInOrder, requestParams);
                preparedQuery = connection.prepareStatement(preparedSql);
                bindPreparedStatementParams(preparedQuery,
                        mustacheKeysInOrder,
                        requestParams
                );

                isResultSet = preparedQuery.execute();
                resultSet = preparedQuery.getResultSet();
            } else {
                statement = connection.createStatement();
                isResultSet = statement.execute(renderMustacheString(query, requestParams), Statement.RETURN_GENERATED_KEYS);
                resultSet = statement.getResultSet();
            }

            return parseExecuteResult(isPreparedStatement, statement, resultSet, preparedQuery, isResultSet);

        } catch (SQLException e) {
            throw new PluginException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e.getMessage());
        } finally {
            releaseResources(connection, statement, resultSet, preparedQuery);
        }
    }

    /**
     * 解析执行结果。
     *
     * @param preparedStatement 是否使用预处理语句
     * @param statement         JDBC语句对象
     * @param resultSet         结果集
     * @param preparedQuery     预处理查询对象
     * @param isResultSet       是否为结果集
     * @return 查询执行结果
     * @throws SQLException SQL异常
     */
    private QueryExecutionResult parseExecuteResult(boolean preparedStatement, Statement statement,
                                                    ResultSet resultSet, PreparedStatement preparedQuery, boolean isResultSet) throws SQLException {

        if (isResultSet) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            List<Map<String, Object>> dataRows = parseRows(resultSet);

            List<String> columnLabels = parseColumns(metaData);
            return QueryExecutionResult.success((dataRows), getHintMessages(columnLabels));
        }

        Object affectedRows = preparedStatement ? Math.max(preparedQuery.getUpdateCount(), 0) // might return -1
                : Math.max(statement.getUpdateCount(), 0);
        Map<String, Object> result = newLinkedHashMap();
        result.put("affectedRows", affectedRows);
        return QueryExecutionResult.success(result);
    }

    /**
     * 获取提示消息。
     *
     * @param columnNames 列名列表
     * @return 提示消息列表
     */
    private List<LocaleMessage> getHintMessages(List<String> columnNames) {
        List<LocaleMessage> messages = new ArrayList<>();
        List<String> identicalColumns = getIdenticalColumns(columnNames);
        if (CollectionUtils.isNotEmpty(identicalColumns)) {
            messages.add(new LocaleMessage("DUPLICATE_COLUMN", String.join("/", identicalColumns)));
        }
        return messages;
    }

    /**
     * 绑定预处理语句参数。
     *
     * @param preparedStatement   预处理语句对象
     * @param mustacheKeysInOrder Mustache键列表
     * @param requestParams       请求参数
     */
    private void bindPreparedStatementParams(PreparedStatement preparedStatement, List<String> mustacheKeysInOrder,
                                             Map<String, Object> requestParams) {

        try {
            for (int index = 0; index < mustacheKeysInOrder.size(); index++) {

                String mustacheKey = mustacheKeysInOrder.get(index);
                Object value = requestParams.get(mustacheKey);

                int bindIndex = index + 1;
                if (value == null) {
                    preparedStatement.setNull(bindIndex, Types.NULL);
                    continue;
                }
                if (value instanceof Integer intValue) {
                    preparedStatement.setInt(bindIndex, intValue);
                    continue;
                }
                if (value instanceof Long longValue) {
                    preparedStatement.setLong(bindIndex, longValue);
                    continue;
                }
                if (value instanceof Float || value instanceof Double) {
                    preparedStatement.setBigDecimal(bindIndex, new BigDecimal(String.valueOf(value)));
                    continue;
                }
                if (value instanceof Boolean boolValue) {
                    preparedStatement.setBoolean(bindIndex, boolValue);
                    continue;
                }
                if (value instanceof Map<?, ?> || value instanceof Collection<?>) {
                    preparedStatement.setString(bindIndex, toJson(value));
                    continue;
                }
                if (value instanceof String strValue) {
                    preparedStatement.setString(bindIndex, strValue);
                    continue;
                }
                throw new PluginException(PREPARED_STATEMENT_BIND_PARAMETERS_ERROR, "PS_BIND_ERROR",
                        mustacheKey, value.getClass().getSimpleName());
            }
        } catch (Exception e) {
            if (e instanceof PluginException pluginException) {
                throw pluginException;
            }
            throw new PluginException(PREPARED_STATEMENT_BIND_PARAMETERS_ERROR, "PREPARED_STATEMENT_BIND_PARAMETERS_ERROR", e.getMessage());
        }
    }

    /**
     * 释放资源。
     *
     * @param autoCloseables 自动关闭对象数组
     */
    private void releaseResources(AutoCloseable... autoCloseables) {
        for (AutoCloseable closeable : autoCloseables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    log.error("close {} error", closeable.getClass().getSimpleName(), e);
                }
            }
        }
    }

    /**
     * 获取数据库连接。
     *
     * @param hikariDataSource Hikari数据源
     * @return 数据库连接对象
     */
    private Connection getConnection(HikariDataSource hikariDataSource) {
        Connection connection;
        try {
            if (hikariDataSource == null || hikariDataSource.isClosed() || !hikariDataSource.isRunning()) {
                throw new InvalidHikariDatasourceException();
            }
            connection = hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new PluginException(CONNECTION_ERROR, "CONNECTION_ERROR", e.getMessage());
        }
        return connection;
    }

}

package com.barda.plugin.sql;

import static com.barda.sdk.exception.PluginCommonError.CONNECTION_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.util.ExceptionUtils.wrapException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.barda.sdk.models.DatasourceConnectionConfig;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.barda.sdk.exception.InvalidHikariDatasourceException;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.plugin.common.BlockingQueryExecutor;
import com.barda.sdk.plugin.common.SqlQueryUtils;
import com.barda.sdk.plugin.common.sql.HikariPerfWrapper;
import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;
import com.barda.sdk.plugin.common.sql.SqlBasedQueryExecutionContext;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.query.QueryVisitorContext;
import com.barda.sdk.util.MustacheHelper;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 基于 SQL 的查询执行器，提供了对 SQL 查询的基本执行支持。
 * <p>
 * SqlBasedDatasourceConnectionConfig SQL 数据源连接配置类型
 * <HikariPerfWrapper>                Hikari 数据源包装器类型
 * <SqlBasedQueryExecutionContext>     SQL 查询执行上下文类型
 */
@Slf4j
public abstract class SqlBasedQueryExecutor extends BlockingQueryExecutor<SqlBasedDatasourceConnectionConfig,
        HikariPerfWrapper, SqlBasedQueryExecutionContext> {

    private final GeneralSqlExecutor generalSqlExecutor;

    /**
     * 构造一个 SqlBasedQueryExecutor 对象。
     *
     * @param generalSqlExecutor 通用 SQL 执行器，不能为空
     */
    protected SqlBasedQueryExecutor(GeneralSqlExecutor generalSqlExecutor) {
        this.generalSqlExecutor = generalSqlExecutor;
    }

    /**
     * 构建查询执行上下文的Mono。该方法应当用于构建查询执行上下文。
     *
     * @param datasourceConnectionConfig 数据源连接配置
     * @param queryConfig                查询配置
     * @param requestParams              请求参数
     * @param queryVisitorContext        查询访问器上下文
     * @return 查询执行上下文的Mono
     */
    @Override
    public Mono<SqlBasedQueryExecutionContext> buildQueryExecutionContextMono(DatasourceConnectionConfig datasourceConnectionConfig, Map<String, Object> queryConfig, Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {
        SqlQueryConfig sqlQueryConfig = SqlQueryConfig.from(queryConfig);
        if (sqlQueryConfig.isGuiMode()) {
            GuiSqlCommand sqlCommand = getGuiSqlCommand(sqlQueryConfig);
            return Mono.just(SqlBasedQueryExecutionContext.builder()
                    .guiSqlCommand(sqlCommand)
                    .requestParams(requestParams)
                    .build());
        }

        String query = SqlQueryUtils.removeQueryComments(sqlQueryConfig.getSql());
        if (StringUtils.isBlank(query)) {
            throw new PluginException(QUERY_ARGUMENT_ERROR, "SQL_EMPTY");
        }

        return Mono.just(SqlBasedQueryExecutionContext.builder()
                .query(query)
                .requestParams(requestParams)
                .disablePreparedStatement(((SqlBasedDatasourceConnectionConfig) datasourceConnectionConfig).isEnableTurnOffPreparedStatement() &&
                        sqlQueryConfig.isDisablePreparedStatement())
                .build());
    }

    /**
     * 获取 GUI SQL 命令对象。
     *
     * @param sqlQueryConfig SQL 查询配置
     * @return 解析得到的 GUI SQL 命令对象
     * @throws PluginException 如果 GUI SQL 命令类型为空或 GUI 参数无效，则抛出异常
     */
    private GuiSqlCommand getGuiSqlCommand(SqlQueryConfig sqlQueryConfig) {
        String guiStatementType = sqlQueryConfig.getGuiStatementType();
        if (StringUtils.isBlank(guiStatementType)) {
            throw new PluginException(QUERY_ARGUMENT_ERROR, "GUI_COMMAND_TYPE_EMPTY");
        }
        Map<String, Object> guiStatementDetail = sqlQueryConfig.getGuiStatementDetail();
        if (MapUtils.isEmpty(guiStatementDetail)) {
            throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_GUI_PARAM");
        }

        return parseSqlCommand(guiStatementType, guiStatementDetail);
    }

    /**
     * 解析 GUI SQL 命令。
     *
     * @param guiStatementType GUI SQL 命令类型
     * @param detail           GUI SQL 命令详情
     * @return 解析得到的 GUI SQL 命令对象
     */
    protected abstract GuiSqlCommand parseSqlCommand(String guiStatementType, Map<String, Object> detail);

    /**
     * 执行 SQL 查询。
     *
     * @param hikariPerfWrapper Hikari 数据源包装器
     * @param context           SQL 查询执行上下文
     * @return 查询执行结果
     * @throws PluginException 如果查询执行出错，则抛出异常
     */
    @Nonnull
    @Override
    protected QueryExecutionResult blockingExecuteQuery(HikariPerfWrapper hikariPerfWrapper, SqlBasedQueryExecutionContext context) {
        HikariDataSource dataSource = getHikariDataSource(hikariPerfWrapper);
        log.info("Hikari hashcode: {}, active: {}, idle: {}, wait: {}, total: {}", dataSource.hashCode()
                , dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
                dataSource.getHikariPoolMXBean().getTotalConnections()
        );

        try (Connection connection = getConnection(dataSource)) {
            return generalSqlExecutor.execute(connection, context);
        } catch (SQLException e) {
            throw wrapException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e);
        }
    }

    private HikariDataSource getHikariDataSource(HikariPerfWrapper hikariDataSource) {
        return (HikariDataSource) hikariDataSource.getHikariDataSource();
    }

    /**
     * 获取数据库结构信息。
     *
     * @param hikariPerfWrapper Hikari 数据源包装器
     * @param connectionConfig  SQL 数据源连接配置
     * @return 数据库结构信息
     * @throws PluginException 如果获取数据库结构信息失败，则抛出异常
     */
    @Nonnull
    @Override
    public final DatasourceStructure blockingGetStructure(HikariPerfWrapper hikariPerfWrapper, SqlBasedDatasourceConnectionConfig connectionConfig) {
        HikariDataSource hikariDataSource = getHikariDataSource(hikariPerfWrapper);
        try (Connection connection = getConnection(hikariDataSource)) {
            return getDatabaseMetadata(connection, connectionConfig);
        } catch (SQLException e) {
            throw wrapException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e);
        }
    }

    /**
     * 获取数据库元数据信息。
     *
     * @param connection       数据库连接
     * @param connectionConfig SQL 数据源连接配置
     * @return 数据库结构信息
     * @throws PluginException 如果获取数据库结构信息失败，则抛出异常
     */
    protected abstract DatasourceStructure getDatabaseMetadata(Connection connection,
                                                               SqlBasedDatasourceConnectionConfig connectionConfig);

    /**
     * 对查询配置进行预处理。
     *
     * @param configMap 查询配置信息
     * @return 预处理后的查询配置
     */
    @Override
    public Map<String, Object> sanitizeQueryConfig(Map<String, Object> configMap) {
        SqlQueryConfig queryConfig = SqlQueryConfig.from(configMap);
        Map<String, Object> result = Maps.newHashMap();
        if (queryConfig.isGuiMode()) {
            GuiSqlCommand guiSqlCommand = getGuiSqlCommand(queryConfig);
            result.put("fields", guiSqlCommand.extractMustacheKeys());
            return result;
        }

        String sql = queryConfig.getSql();
        Set<String> mustacheKeys = MustacheHelper.extractMustacheKeysWithCurlyBraces(sql);
        result.put("fields", mustacheKeys);
        return result;
    }

    /**
     * 获取数据库连接。
     *
     * @param hikariDataSource Hikari 数据源对象
     * @return 数据库连接
     * @throws PluginException 如果获取连接失败，则抛出异常
     */
    private Connection getConnection(HikariDataSource hikariDataSource) {
        try {
            if (hikariDataSource == null || hikariDataSource.isClosed() || !hikariDataSource.isRunning()) {
                throw new InvalidHikariDatasourceException();
            }
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            throw new PluginException(CONNECTION_ERROR, "CONNECTION_ERROR", e.getMessage());
        }
    }

}
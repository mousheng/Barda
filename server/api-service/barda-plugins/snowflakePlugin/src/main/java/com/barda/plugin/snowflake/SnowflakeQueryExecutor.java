package com.barda.plugin.snowflake;

import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_GET_STRUCTURE_ERROR;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import com.barda.plugin.sql.GeneralSqlExecutor;
import com.barda.plugin.sql.SqlBasedQueryExecutor;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.models.DatasourceStructure.Column;
import com.barda.sdk.models.DatasourceStructure.Table;
import com.barda.sdk.models.DatasourceStructure.TableType;
import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;
import com.barda.sdk.plugin.common.sql.SqlBasedQueryExecutionContext;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.query.QueryVisitorContext;
import com.barda.sdk.util.ExceptionUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Snowflake 数据库查询执行器，继承自基于 SQL 的查询执行器。
 */
@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
@Slf4j
@Extension
public class SnowflakeQueryExecutor extends SqlBasedQueryExecutor {

    /**
     * SnowflakeQueryExecutor 的构造方法。
     */
    public SnowflakeQueryExecutor() {
        super(new GeneralSqlExecutor(false));
    }

    /**
     * 查询数据库列的 SQL。
     */
    @SuppressWarnings("SqlDialectInspection")
    public static final String COLUMNS_QUERY = """
            SELECT
               table_schema as "table_schema",
               concat(table_schema, '.', table_name) as "table_name",
               column_name as "column_name",
               data_type as "column_type"
               FROM INFORMATION_SCHEMA.COLUMNS
               where table_schema = '#SCHEMA'
               ORDER BY table_name, ordinal_position""";

    /**
     * 不包含 schema 的列查询 SQL。
     */
    @SuppressWarnings("SqlDialectInspection")
    private static final String COLUMNS_QUERY_WITHOUT_SCHEMA = """
            SELECT
             table_schema as "table_schema",
               concat(table_schema, '.', table_name) as "table_name",
               column_name as "column_name",
               data_type as "column_type"
               FROM INFORMATION_SCHEMA.COLUMNS
               ORDER BY table_name, ordinal_position
             """;

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
    public Mono<SqlBasedQueryExecutionContext> doBuildQueryExecutionContextMono(SqlBasedDatasourceConnectionConfig connectionConfig, Map<String, Object> queryConfig, Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {
        return super.doBuildQueryExecutionContextMono(connectionConfig, queryConfig, requestParams, queryVisitorContext);
    }

    /**
     * 获取数据库元数据信息。
     *
     * @param connection       数据库连接
     * @param connectionConfig SQL 数据源连接配置
     * @return 数据库结构信息
     */
    @Nonnull
    @Override
    protected DatasourceStructure getDatabaseMetadata(Connection connection, SqlBasedDatasourceConnectionConfig connectionConfig) {
        Map<String, Table> tablesByName = new LinkedHashMap<>();

        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getTableSchemaQuery(connectionConfig))) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("table_name");
                String schema = resultSet.getString("table_schema");
                Table table = tablesByName.computeIfAbsent(tableName, __ -> new Table(
                        TableType.TABLE, schema, tableName,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                ));

                table.addColumn(new Column(
                        resultSet.getString("column_name"),
                        resultSet.getString("column_type"),
                        null,
                        false
                ));
            }
        } catch (SQLException throwable) {
            throw ExceptionUtils.wrapException(DATASOURCE_GET_STRUCTURE_ERROR, "DATASOURCE_GET_STRUCTURE_ERROR", throwable);
        }
        return new DatasourceStructure(new ArrayList<>(tablesByName.values()));
    }

    /**
     * 获取数据库列查询 SQL。
     *
     * @param connectionConfig SQL 数据源连接配置
     * @return 数据库列查询 SQL
     */
    private static String getTableSchemaQuery(SqlBasedDatasourceConnectionConfig connectionConfig) {
        Object schema = connectionConfig.getExtParams().get("schema");

        if (schema != null && StringUtils.isNotBlank(String.valueOf(schema))) {
            return COLUMNS_QUERY.replace("#SCHEMA", String.valueOf(schema));
        }
        return COLUMNS_QUERY_WITHOUT_SCHEMA;
    }

    /**
     * 解析 GUI SQL 命令。
     * SnowflakeQueryExecutor 不支持此操作，因此抛出异常。
     *
     * @param guiStatementType GUI SQL 命令类型
     * @param detail           GUI SQL 命令详情
     * @return 不支持该操作，抛出 UnsupportedOperationException
     * @throws UnsupportedOperationException 总是抛出此异常
     */
    @Override
    protected GuiSqlCommand parseSqlCommand(String guiStatementType, Map<String, Object> detail) {
        throw new UnsupportedOperationException();
    }

}

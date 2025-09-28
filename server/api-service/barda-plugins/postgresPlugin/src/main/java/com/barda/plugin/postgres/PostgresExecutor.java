package com.barda.plugin.postgres;

import static com.google.common.collect.Lists.newArrayList;
import static com.barda.plugin.postgres.utils.PostgresDataTypeUtils.castValueWithTargetType;
import static com.barda.plugin.postgres.utils.PostgresDataTypeUtils.extractExplicitCasting;
import static com.barda.plugin.postgres.utils.PostgresResultParser.parseDatabaseStructure;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.util.MustacheHelper.doPrepareStatement;
import static com.barda.sdk.util.MustacheHelper.extractMustacheKeysInOrder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.pf4j.Extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.barda.plugin.postgres.model.DataType;
import com.barda.plugin.postgres.utils.PostgresResultParser;
import com.barda.plugin.sql.GeneralSqlExecutor;
import com.barda.plugin.sql.SqlBasedQueryExecutor;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.command.postgres.PostgresBulkInsertCommand;
import com.barda.sdk.plugin.sqlcommand.command.postgres.PostgresBulkUpdateCommand;
import com.barda.sdk.plugin.sqlcommand.command.postgres.PostgresDeleteCommand;
import com.barda.sdk.plugin.sqlcommand.command.postgres.PostgresInsertCommand;
import com.barda.sdk.plugin.sqlcommand.command.postgres.PostgresUpdateCommand;

import lombok.extern.slf4j.Slf4j;

/**
 * 扩展自SqlBasedQueryExecutor的PostgresExecutor类，用于执行PostgresSQL查询。
 * 该类提供PostgresSQL特定的功能，如解析结果、准备SQL语句等。
 */
@Slf4j
@Extension
public class PostgresExecutor extends SqlBasedQueryExecutor {

    public PostgresExecutor() {
        super(new GeneralSqlExecutor() {

            /**
             * 重写父类GeneralSqlExecutor的parseDataRows方法，用于解析ResultSet并返回行数据。
             *
             * @param resultSet 要解析的ResultSet
             * @return 行数据列表
             * @throws SQLException 若发生SQL异常
             */
            @Override
            protected List<Map<String, Object>> parseDataRows(ResultSet resultSet) throws SQLException {
                try {
                    // 使用PostgresResultParser解析ResultSet并返回行数据
                    return PostgresResultParser.parseRows(resultSet);
                } catch (JsonProcessingException e) {
                    // 捕获JSON处理异常并抛出插件异常
                    throw new PluginException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e.getMessage());
                }
            }

            /**
             * 重写父类GeneralSqlExecutor的getPreparedStatementInput方法，用于准备SQL语句并返回StatementInput。
             *
             * @param query           原始SQL查询
             * @param requestParams   请求参数
             * @return 准备好的StatementInput
             */
            @Override
            protected StatementInput getPreparedStatementInput(String query, Map<String, Object> requestParams) {
                List<String> mustacheKeysInOrder = extractMustacheKeysInOrder(query);
                String preparedSql = doPrepareStatement(query, mustacheKeysInOrder, requestParams);

                if (mustacheKeysInOrder.isEmpty()) {
                    // 若没有mustache键，则直接返回StatementInput
                    return StatementInput.fromSql(true, preparedSql, Collections.emptyList());
                }

                List<DataType> explicitCastDataTypes = extractExplicitCasting(preparedSql);
                List<Object> finalValues = convertExplicitDataTypes(requestParams, mustacheKeysInOrder, explicitCastDataTypes);
                return StatementInput.fromSql(true, preparedSql, finalValues);
            }

            /**
             * 转换显式数据类型并返回一个包含转换后值的列表。
             *
             * @param requestParams 包含请求参数的Map
             * @param mustacheKeysInOrder 按Mustache模板中定义的顺序排列的键列表
             * @param explicitCastDataTypes 显式数据类型列表
             * @return 包含转换后值的列表
             * @throws PluginException 如果绑定值不匹配，则抛出插件异常
             */
            private List<Object> convertExplicitDataTypes(Map<String, Object> requestParams, List<String> mustacheKeysInOrder,
                    List<DataType> explicitCastDataTypes) {
                List<Object> finalValues = newArrayList();
                for (int i = 0; i < mustacheKeysInOrder.size(); i++) {
                    String key = mustacheKeysInOrder.get(i);
                    boolean containsKey = requestParams.containsKey(key);
                    if (!containsKey) {
                        // 若绑定值不匹配，则抛出插件异常
                        throw new PluginException(QUERY_EXECUTION_ERROR, "BOUND_VALUE_NOT_MATCH", key);
                    }

                    Object value = requestParams.get(key);
                    DataType targetType = explicitCastDataTypes.get(i);
                    if (targetType != null) {
                        finalValues.add(castValueWithTargetType(value, explicitCastDataTypes.get(i)));
                    } else {
                        finalValues.add(value);
                    }
                }
                return finalValues;
            }

        });
    }

    /**
     * 解析GUI命令并返回对应的SQL命令。
     *
     * @param guiStatementType GUI命令类型
     * @param detail           GUI命令的详细信息
     * @return 对应的SQL命令
     */
    protected GuiSqlCommand parseSqlCommand(String guiStatementType, Map<String, Object> detail) {
        return switch (guiStatementType.toUpperCase()) {
            case "INSERT" -> PostgresInsertCommand.from(detail);
            case "UPDATE" -> PostgresUpdateCommand.from(detail);
            case "DELETE" -> PostgresDeleteCommand.from(detail);
            case "BULK_INSERT" -> PostgresBulkInsertCommand.from(detail);
            case "BULK_UPDATE" -> PostgresBulkUpdateCommand.from(detail);
            default -> throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_GUI_COMMAND_TYPE", guiStatementType);
        };
    }

    /**
     * 获取数据库元数据并返回。
     *
     * @param connection       数据库连接
     * @param connectionConfig 数据库连接配置
     * @return 数据库元数据
     */
    @Override
    protected DatasourceStructure getDatabaseMetadata(Connection connection, SqlBasedDatasourceConnectionConfig connectionConfig) {
        DatasourceStructure structure = new DatasourceStructure();
        Map<String, DatasourceStructure.Table> tablesByName = new LinkedHashMap<>();

        try (Statement statement = connection.createStatement()) {
            parseDatabaseStructure(tablesByName, statement);
        } catch (SQLException throwable) {
            // 捕获SQL异常并抛出插件异常
            throw new PluginException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", throwable.getMessage());
        }

        structure.setTables(new ArrayList<>(tablesByName.values()));
        for (DatasourceStructure.Table table : structure.getTables()) {
            table.getKeys().sort(Comparator.naturalOrder());
        }
        return structure;
    }

}

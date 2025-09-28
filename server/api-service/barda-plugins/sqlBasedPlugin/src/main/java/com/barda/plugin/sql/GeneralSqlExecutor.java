package com.barda.plugin.sql;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.barda.sdk.exception.PluginCommonError.PREPARED_STATEMENT_BIND_PARAMETERS_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.util.ExceptionUtils.wrapException;
import static com.barda.sdk.util.JsonUtils.toJson;
import static com.barda.sdk.util.MustacheHelper.doPrepareStatement;
import static com.barda.sdk.util.MustacheHelper.extractMustacheKeysInOrder;
import static com.barda.sdk.util.MustacheHelper.renderMustacheString;
import static java.util.Collections.emptyList;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;

import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.plugin.common.sql.ResultSetParser;
import com.barda.sdk.plugin.common.sql.SqlBasedQueryExecutionContext;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand.GuiSqlCommandRenderResult;
import com.barda.sdk.plugin.sqlcommand.command.UpdateOrDeleteSingleCommandRenderResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneralSqlExecutor {

    private final boolean supportGenerateKeys;

    /**
     * 使用指定的是否支持生成键值来构造 GeneralSqlExecutor。
     *
     * @param supportGenerateKeys 如果为 true，执行器将支持生成键值；否则不支持。
     */
    public GeneralSqlExecutor(boolean supportGenerateKeys) {
        this.supportGenerateKeys = supportGenerateKeys;
    }

    /**
     * 构造一个默认支持生成键值的 GeneralSqlExecutor。
     */
    public GeneralSqlExecutor() {
        this(true);
    }

    /**
     * 使用提供的连接和执行上下文执行SQL查询。
     *
     * @param connection 数据库连接，不能为null
     * @param context    提供查询执行详细信息的上下文，不能为null
     * @return 查询执行结果
     * @throws NullPointerException 如果连接或上下文为null
     */
    @Nonnull
    public final QueryExecutionResult execute(Connection connection, SqlBasedQueryExecutionContext context) {

        GuiSqlCommand guiSqlCommand = context.getGuiSqlCommand();
        boolean guiMode = guiSqlCommand != null;
        String query = context.getQuery();
        boolean isPreparedStatement = guiMode || !context.isDisablePreparedStatement();
        Map<String, Object> requestParams = new HashMap<>(context.getRequestParams());

        StatementInput statementInput = getSqlExecutionInput(guiSqlCommand, query, isPreparedStatement, requestParams);
        return doExecute(connection, statementInput);
    }

    /**
     * 执行 SQL 查询并返回执行结果。
     *
     * @param connection     用于执行查询的数据库连接，不能为null
     * @param statementInput 包含查询执行所需信息的 StatementInput 对象，不能为null
     * @return 查询执行结果
     * @throws SQLException 如果在执行查询时发生数据库访问错误
     * @throws NullPointerException 如果 connection 或 statementInput 为 null
     */
    private QueryExecutionResult doExecute(Connection connection, StatementInput statementInput) {
        Pair<Statement, Boolean> executionResult = getStatementAndExecute(connection, statementInput);

        boolean isResultSet = executionResult.getRight();
        try (Statement statement = executionResult.getLeft()) {
            return parseExecuteResult(statement, isResultSet);
        } catch (SQLException e) {
            throw wrapException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e);
        }
    }

    /**
     * 解析执行结果并返回一个 QueryExecutionResult 对象。
     *
     * @param statement   用于获取执行结果的 SQL Statement 对象，不能为 null
     * @param isResultSet 指示执行结果是否为 ResultSet
     * @return 封装了执行结果的 QueryExecutionResult 对象
     * @throws SQLException 如果在解析执行结果时发生数据库访问错误
     */
    private QueryExecutionResult parseExecuteResult(Statement statement, boolean isResultSet) throws SQLException {

        List<Object> result = newArrayList();
        int updateCount = statement.getUpdateCount();
        do {
            if (isResultSet) {
                try (ResultSet resultSet = statement.getResultSet()) {
                    List<Map<String, Object>> dataRows = parseDataRows(resultSet);
                    if (!isGeneratedKeysWithNullValue(dataRows)) {
                        result.add(dataRows);
                    }
                }
            } else {
                result.add(getAffectRowsAndGeneratedKeys(statement, updateCount));
            }

            isResultSet = statement.getMoreResults();
            updateCount = statement.getUpdateCount();
        } while (isResultSet || updateCount != -1);

        if (result.size() == 1) {
            return QueryExecutionResult.success(result.get(0));
        }

        return QueryExecutionResult.success(result);
    }

    /**
     * 解析 ResultSet 并返回数据行的列表。
     *
     * @param resultSet 要解析的 ResultSet，不能为 null
     * @return 包含数据行的列表，每行表示为一个 Map，其中键是列名，值是列值
     * @throws SQLException 如果在解析 ResultSet 时发生数据库访问错误
     */
    protected List<Map<String, Object>> parseDataRows(ResultSet resultSet) throws SQLException {
        return ResultSetParser.parseRows(resultSet);
    }

    /**
     * 获取受影响的行数和生成的键值。
     *
     * @param statement   用于执行 SQL 查询的 Statement 对象，不能为 null
     * @param updateCount 受影响的行数
     * @return 包含受影响的行数和生成键值的 Map 对象
     * @throws SQLException 如果在获取生成键值时发生数据库访问错误
     */
    private Map<String, Object> getAffectRowsAndGeneratedKeys(Statement statement, int updateCount) throws SQLException {
        Map<String, Object> result = newHashMapWithExpectedSize(2);
        result.put("affectedRows", updateCount);

        ResultSet generatedKeys = getGeneratedKeys(statement);
        if (generatedKeys == null) {
            return result;
        }

        try (generatedKeys) {
            List<Object> generatedIds = getGeneratedIds(generatedKeys);
            if (!generatedIds.isEmpty()) {
                result.put("generatedKeys", generatedIds);
            }
        }
        return result;
    }

    /**
     * 获取生成的键值 ResultSet 对象。
     *
     * @param statement 用于执行 SQL 查询的 Statement 对象，不能为 null
     * @return 包含生成键值的 ResultSet 对象，或在某些情况下为 null
     */
    private static ResultSet getGeneratedKeys(Statement statement) {
        try {
            return statement.getGeneratedKeys();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查数据行是否仅包含 null 值的生成键。
     *
     * @param dataRows 要检查的数据行
     * @return 如果数据行仅包含 null 值的生成键，则返回 true；否则返回 false
     */
    private static boolean isGeneratedKeysWithNullValue(List<Map<String, Object>> dataRows) {
        if (dataRows.size() != 1) {
            return false;
        }
        Map<String, Object> map = dataRows.get(0);
        if (map.size() == 1) {
            return map.containsKey("GENERATED_KEYS");
        }
        return false;
    }

    /**
     * 执行给定的 SQL 语句，并返回包含 Statement 和是否是 ResultSet 的 Pair 对象。
     *
     * @param connection     用于执行 SQL 语句的数据库连接，不能为 null
     * @param statementInput 包含 SQL 语句和参数的 StatementInput 对象，不能为 null
     * @return 包含 Statement 和是否是 ResultSet 的 Pair 对象
     * @throws PluginException 如果在执行 SQL 语句时发生错误或单行命令影响了多行数据
     */
    private Pair<Statement, Boolean> getStatementAndExecute(Connection connection, StatementInput statementInput) {
        if (statementInput instanceof UpdateOrDeleteSingleRowStatementInput comboInput) {
            StatementInput selectInput = comboInput.getSelectInput();
            QueryExecutionResult selectResult = doExecute(connection, selectInput);
            int selectCount = getSelectCount(selectResult);
            if (selectCount > 1) {
                throw new PluginException(QUERY_EXECUTION_ERROR, "AFFECT_MORE_THAN_ONE_ROWS_FOR_SINGLE_COMMAND");
            }
        }

        try {
            if (statementInput.isPreparedStatement()) {
                String sql = statementInput.getSql();
                List<Object> params = statementInput.getParams();
                var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                bindPreparedStatementParams(statement, params);
                var isResultSet = statement.execute();
                return Pair.of(statement, isResultSet);
            }

            var statement = connection.createStatement();
            boolean isResultSet;
            if (supportGenerateKeys) {
                isResultSet = statement.execute(statementInput.getSql(), Statement.RETURN_GENERATED_KEYS);
            } else {
                isResultSet = statement.execute(statementInput.getSql());
            }
            return Pair.of(statement, isResultSet);
        } catch (Exception e) {
            throw wrapException(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e);
        }
    }

    /**
     * 获取查询结果中的行数。
     *
     * @param queryExecutionResult 查询执行结果对象，不能为 null
     * @return 查询结果中的行数
     * @throws PluginException 如果无法获取受影响的行数
     */
    @SuppressWarnings("unchecked")
    private static int getSelectCount(QueryExecutionResult queryExecutionResult) {
        List<Map<String, Object>> selectResult = (List<Map<String, Object>>) queryExecutionResult.getData();
        if (selectResult.get(0).get("count") instanceof Number count) {
            return count.intValue();
        }
        throw new PluginException(QUERY_EXECUTION_ERROR, "FAIL_TO_GET_AFFECTED_ROW_COUNT");
    }

    /**
     * 获取 SQL 执行输入对象。
     *
     * @param guiSqlCommand     GUI SQL 命令对象，可以为 null
     * @param query             SQL 查询字符串，不能为 null
     * @param isPreparedStatement 是否使用 PreparedStatement
     * @param requestParams     请求参数，不能为 null
     * @return 包含 SQL 语句和参数的 StatementInput 对象
     */
    private StatementInput getSqlExecutionInput(GuiSqlCommand guiSqlCommand, String query, boolean isPreparedStatement,
            Map<String, Object> requestParams) {
        if (isPreparedStatement) {
            return getPreparedStatementSqlInput(guiSqlCommand, query, requestParams);
        }
        String renderedSql = renderMustacheString(query, requestParams);
        return StatementInput.fromSql(false, renderedSql, emptyList());
    }

    /**
     * 获取 PreparedStatement 的 SQL 执行输入对象。
     *
     * @param guiSqlCommand GUI SQL 命令对象，可以为 null
     * @param query         SQL 查询字符串，不能为 null
     * @param requestParams 请求参数，不能为 null
     * @return 包含 SQL 语句和参数的 StatementInput 对象
     */
    private StatementInput getPreparedStatementSqlInput(GuiSqlCommand guiSqlCommand, String query, Map<String, Object> requestParams) {
        if (guiSqlCommand != null) {
            GuiSqlCommandRenderResult renderResult = guiSqlCommand.render(requestParams);
            if (renderResult instanceof UpdateOrDeleteSingleCommandRenderResult updateOrDeleteSingle) {
                return StatementInput.fromUpdateOrDeleteSingleRowSql(updateOrDeleteSingle);
            }

            return StatementInput.fromSql(true, renderResult.sql(), renderResult.bindParams());
        }

        return getPreparedStatementInput(query, requestParams);
    }

    /**
     * 获取 PreparedStatement 的 SQL 执行输入对象。
     *
     * @param query         SQL 查询字符串，不能为 null
     * @param requestParams 请求参数，不能为 null
     * @return 包含 SQL 语句和参数的 StatementInput 对象
     */
    protected StatementInput getPreparedStatementInput(String query, Map<String, Object> requestParams) {
        List<String> mustacheKeysInOrder = extractMustacheKeysInOrder(query);
        String preparedSql = doPrepareStatement(query, mustacheKeysInOrder, requestParams);
        List<Object> bindParams = mustacheKeysInOrder.stream()
                .map(requestParams::get)
                .toList();
        return StatementInput.fromSql(true, preparedSql, bindParams);
    }

    /**
     * 绑定 PreparedStatement 的参数。
     *
     * @param preparedQuery PreparedStatement 对象，不能为 null
     * @param bindParams    参数列表，不能为 null
     * @throws PluginException 如果在绑定参数时发生错误
     */
    private void bindPreparedStatementParams(PreparedStatement preparedQuery, List<Object> bindParams) {
        try {
            for (int index = 0; index < bindParams.size(); index++) {
                Object value = bindParams.get(index);
                bindParam(index + 1, value, preparedQuery, "");
            }
        } catch (Exception e) {
            throw wrapException(PREPARED_STATEMENT_BIND_PARAMETERS_ERROR, "PREPARED_STATEMENT_BIND_PARAMETERS_ERROR", e);
        }
    }

    /**
     * 获取生成的键值 ID 列表。
     *
     * @param generatedKeys 包含生成键值的 ResultSet 对象，不能为 null
     * @return 生成键值的 ID 列表
     * @throws SQLException 如果在获取生成键值时发生数据库访问错误
     */
    private List<Object> getGeneratedIds(ResultSet generatedKeys) throws SQLException {
        if (generatedKeys == null) {
            return emptyList();
        }
        List<Object> array = newArrayList();
        while (generatedKeys.next()) {
            array.add(generatedKeys.getObject(1));
        }
        return array;
    }

    /**
     * 绑定参数到 PreparedStatement 对象。
     *
     * @param bindIndex       参数索引，从 1 开始，不能为负数
     * @param value           参数值，可以为 null
     * @param preparedStatement 用于绑定参数的 PreparedStatement 对象，不能为 null
     * @param bindKeyName     绑定参数的键名称，可以为 null
     * @throws SQLException 如果在绑定参数时发生数据库访问错误
     * @throws PluginException 如果参数类型不受支持
     */
    private void bindParam(int bindIndex, Object value, PreparedStatement preparedStatement, String bindKeyName) throws SQLException {
        if (value == null) {
            preparedStatement.setNull(bindIndex, Types.NULL);
            return;
        }
        if (value instanceof Integer intValue) {
            preparedStatement.setInt(bindIndex, intValue);
            return;
        }
        if (value instanceof Long longValue) {
            preparedStatement.setLong(bindIndex, longValue);
            return;
        }
        if (value instanceof Float || value instanceof Double) {
            preparedStatement.setBigDecimal(bindIndex, new BigDecimal(String.valueOf(value)));
            return;
        }
        if (value instanceof BigDecimal bigDecimal) {
            preparedStatement.setBigDecimal(bindIndex, bigDecimal);
            return;
        }

        if (value instanceof Boolean boolValue) {
            preparedStatement.setBoolean(bindIndex, boolValue);
            return;
        }
        if (value instanceof Map<?, ?> || value instanceof Collection<?>) {
            preparedStatement.setString(bindIndex, toJson(value));
            return;
        }
        if (value instanceof String strValue) {
            preparedStatement.setString(bindIndex, strValue);
            return;
        }

        if (value instanceof byte[] bytesValue) {
            preparedStatement.setBytes(bindIndex, bytesValue);
            return;
        }

        if (value instanceof Date date) {
            preparedStatement.setDate(bindIndex, date);
            return;
        }

        if (value instanceof Time time) {
            preparedStatement.setTime(bindIndex, time);
            return;
        }

        if (value instanceof Timestamp timestamp) {
            preparedStatement.setTimestamp(bindIndex, timestamp);
            return;
        }

        throw new PluginException(PREPARED_STATEMENT_BIND_PARAMETERS_ERROR, "PS_BIND_ERROR", bindKeyName, value.getClass().getSimpleName());
    }

    /**
 * 这是一个表示 SQL 执行输入的类。它包含了 SQL 语句和参数列表。
 */
    public static class StatementInput {

    /**
     * 指示 SQL 语句是否使用 PreparedStatement 执行。
     */
    private final boolean preparedStatement;
    /**
     * 要执行的 SQL 语句。
     */
    private final String sql;

    /**
     * 要执行的 SQL 语句绑定的参数列表。
     */
    private final List<Object> params;

    /**
     * 私有构造函数，用于创建 StatementInput 对象。
     *
     * @param preparedStatement 指示 SQL 语句是否使用 PreparedStatement 执行
     * @param sql 要执行的 SQL 语句
     * @param params 要执行的 SQL 语句绑定的参数列表
     */
    private StatementInput(boolean preparedStatement, String sql, List<Object> params) {
        this.preparedStatement = preparedStatement;
        this.sql = sql;
        this.params = params;
    }

    /**
     * 创建一个 StatementInput 对象，用于执行 SQL 语句。
     *
     * @param preparedStatement 指示 SQL 语句是否使用 PreparedStatement 执行
     * @param sql 要执行的 SQL 语句
     * @param params 要执行的 SQL 语句绑定的参数列表
     * @return 创建的 StatementInput 对象
     */
    public static StatementInput fromSql(boolean preparedStatement, String sql, List<Object> params) {
        return new StatementInput(preparedStatement, sql, params);
    }

    /**
     * 创建一个 StatementInput 对象，用于执行更新或删除单行记录的 SQL 语句。
     *
     * @param updateOrDeleteSingle 执行的 SQL 语句和参数
     * @return 创建的 StatementInput 对象
     */
    public static StatementInput fromUpdateOrDeleteSingleRowSql(UpdateOrDeleteSingleCommandRenderResult updateOrDeleteSingle) {
        return new UpdateOrDeleteSingleRowStatementInput(updateOrDeleteSingle.sql(), updateOrDeleteSingle.bindParams(),
                updateOrDeleteSingle.getSelectQuery(), updateOrDeleteSingle.getSelectBindParams());
    }

    /**
     * 获取指示 SQL 语句是否使用 PreparedStatement 执行的标志。
     *
     * @return 指示 SQL 语句是否使用 PreparedStatement 执行的标志
     */
    public boolean isPreparedStatement() {
        return preparedStatement;
    }

    /**
     * 获取要执行的 SQL 语句。
     *
     * @return 要执行的 SQL 语句
     */
    public String getSql() {
        return sql;
    }

    /**
     * 获取要执行的 SQL 语句绑定的参数列表。
     *
     * @return 要执行的 SQL 语句绑定的参数列表
     */
    public List<Object> getParams() {
        return params;
    }
    }

    /**
 * 用于更新或删除单行记录的语句输入类。
 * 该类继承自 StatementInput，并添加了用于执行前查询的功能。
 */
public static class UpdateOrDeleteSingleRowStatementInput extends StatementInput {

    /**
     * 执行前查询的 SQL 语句。
     */
    private final String selectSql;

    /**
     * 执行前查询的 SQL 语句绑定的参数列表。
     */
    private final List<Object> selectParams;

    /**
     * 构造函数。
     *
     * @param sql 执行的 SQL 语句
     * @param params 执行的 SQL 语句绑定的参数列表
     * @param selectSql 执行前查询的 SQL 语句
     * @param selectParams 执行前查询的 SQL 语句绑定的参数列表
     */
    private UpdateOrDeleteSingleRowStatementInput(String sql, List<Object> params, String selectSql, List<Object> selectParams) {
        super(true, sql, params);
        this.selectSql = selectSql;
        this.selectParams = selectParams;
    }

    /**
     * 获取执行前查询的 StatementInput 对象。
     *
     * @return 执行前查询的 StatementInput 对象
     */
    public StatementInput getSelectInput() {
        return StatementInput.fromSql(isPreparedStatement(), selectSql(), selectBindParams());
    }

    /**
     * 获取执行前查询的 SQL 语句。
     *
     * @return 执行前查询的 SQL 语句
     */
    public String selectSql() {
        return selectSql;
    }

    /**
     * 获取执行前查询的 SQL 语句绑定的参数列表。
     *
     * @return 执行前查询的 SQL 语句绑定的参数列表
     */
    public List<Object> selectBindParams() {
        return selectParams;
    }
}
}

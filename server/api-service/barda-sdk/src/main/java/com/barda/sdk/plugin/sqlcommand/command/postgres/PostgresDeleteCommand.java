package com.barda.sdk.plugin.sqlcommand.command.postgres;

import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.POSTGRES_COLUMN_DELIMITER;
import static com.barda.sdk.plugin.sqlcommand.filter.FilterSet.parseFilterSet;
import static com.barda.sdk.util.SqlGuiUtils.POSTGRES_SQL_STR_ESCAPE;
import static java.util.Collections.emptyList;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.command.DeleteCommand;
import com.barda.sdk.plugin.sqlcommand.command.UpdateOrDeleteSingleCommandRenderResult;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;
import com.barda.sdk.util.MustacheHelper;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

/**
 * 一个用于执行 PostgreSQL 删除操作的命令类。
 * 继承自 {@link DeleteCommand}，并实现了特定于 PostgreSQL 的功能。
 */
public class PostgresDeleteCommand extends DeleteCommand {

    /**
     * 构造函数。
     *
     * @param table 表名
     * @param filterSet 包含要应用于删除操作的过滤器集合
     * @param allowMultiModify 指示是否允许执行多行修改
     */
    protected PostgresDeleteCommand(String table, FilterSet filterSet, boolean allowMultiModify) {
        super(table, filterSet, allowMultiModify, POSTGRES_COLUMN_DELIMITER, POSTGRES_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息中创建一个 {@link PostgresDeleteCommand} 实例。
     *
     * @param commandDetail 命令详细信息
     * @return 创建的 {@link PostgresDeleteCommand} 实例
     */
    public static DeleteCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        FilterSet filterSet = parseFilterSet(commandDetail);
        boolean allowMultiModify = GuiSqlCommand.parseAllowMultiModify(commandDetail);
        return new PostgresDeleteCommand(table, filterSet, allowMultiModify);
    }

    /**
     * 渲染此命令并返回 SQL 语句和绑定参数。
     *
     * @param requestMap 请求参数
     * @return 渲染结果
     */
    @Override
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {
        String renderedTable = MustacheHelper.renderMustacheString(table, requestMap);

        StringBuilder deleteSql = new StringBuilder();
        deleteSql.append("delete from ").append(renderedTable);
        if (filterSet.isEmpty()) {
            if (!allowMultiModify) {
                return new UpdateOrDeleteSingleCommandRenderResult("select count(1) as count from " + table, emptyList(),
                        deleteSql.toString(), emptyList());
            }
            return new GuiSqlCommandRenderResult(deleteSql.toString(), emptyList());
        }

        GuiSqlCommandRenderResult filterRender = filterSet.render(requestMap, columnFrontDelimiter, columnBackDelimiter, isRenderWithRawSql(),
                escapeStrFunc());
        deleteSql.append(filterRender.sql());

        String selectSql = "select count(1) as count from " + renderedTable + filterRender.sql();

        if (!allowMultiModify) {
            return new UpdateOrDeleteSingleCommandRenderResult(selectSql, filterRender.bindParams(),
                    deleteSql.toString(), filterRender.bindParams());
        }
        return new GuiSqlCommandRenderResult(deleteSql.toString(), filterRender.bindParams());
    }

    /**
     * 指示是否使用原始 SQL 渲染此命令。
     *
     * @return 如果使用原始 SQL 渲染，则返回 true，否则返回 false
     */
    @Override
    public boolean isRenderWithRawSql() {
        return true;
    }

    /**
     * 获取用于转义 SQL 字符串的函数。
     *
     * @return 转义 SQL 字符串的函数
     */
    @Override
    public EscapeSql escapeStrFunc() {
        return POSTGRES_SQL_STR_ESCAPE;
    }
}

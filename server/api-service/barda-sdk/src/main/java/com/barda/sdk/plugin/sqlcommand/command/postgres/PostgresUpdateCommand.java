package com.barda.sdk.plugin.sqlcommand.command.postgres;

import static com.google.common.collect.Lists.newArrayList;
import static com.barda.sdk.exception.PluginCommonError.INVALID_UPDATE_COMMAND;
import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.POSTGRES_COLUMN_DELIMITER;
import static com.barda.sdk.util.MustacheHelper.renderMustacheString;
import static com.barda.sdk.util.SqlGuiUtils.POSTGRES_SQL_STR_ESCAPE;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetRow;
import com.barda.sdk.plugin.sqlcommand.command.UpdateCommand;
import com.barda.sdk.plugin.sqlcommand.command.UpdateOrDeleteSingleCommandRenderResult;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

/**
 * 一个用于在 PostgreSQL 数据库中执行 UPDATE 命令的类。
 * 它继承自 {@link UpdateCommand} 并实现了特定于 PostgreSQL 的功能。
 */
public class PostgresUpdateCommand extends UpdateCommand {

    /**
     * 私有构造函数，用于从命令详细信息创建 {@link PostgresUpdateCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     */
    private PostgresUpdateCommand(Map<String, Object> commandDetail) {
        super(commandDetail, POSTGRES_COLUMN_DELIMITER, POSTGRES_COLUMN_DELIMITER);
    }

    /**
     * 仅用于测试的受保护的构造函数，用于从表名、{@link ChangeSet}、{@link FilterSet} 和 allowMultiModify 创建
     * {@link PostgresUpdateCommand} 实例。
     *
     * @param table 表名
     * @param changeSet 包含更改的 ChangeSet
     * @param filterSet 包含过滤器的 FilterSet
     * @param allowMultiModify 指示是否允许多次修改
     */
    @VisibleForTesting
    protected PostgresUpdateCommand(String table, ChangeSet changeSet, FilterSet filterSet, boolean allowMultiModify) {
        super(table, changeSet, filterSet, allowMultiModify, POSTGRES_COLUMN_DELIMITER, POSTGRES_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息创建一个 {@link PostgresUpdateCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return 新的 {@link PostgresUpdateCommand} 实例
     */
    public static PostgresUpdateCommand from(Map<String, Object> commandDetail) {
        return new PostgresUpdateCommand(commandDetail);
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以提供 PostgreSQL 特定的 SQL 渲染。
     * 如果不允许多次修改，则在执行 UPDATE 之前先执行 SELECT 以检查是否存在要修改的行。
     */
    @Override
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {
        if (allowMultiModify) {
            return super.render(requestMap);
        }

        String renderedTable = renderMustacheString(table, requestMap);
        ChangeSetRow updateRow = changeSet.render(requestMap);
        if (updateRow.isEmpty()) {
            throw new PluginException(INVALID_UPDATE_COMMAND, "UPDATE_DATA_EMPTY");
        }

        StringBuilder selectSql = new StringBuilder("select count(1) as count from " + renderedTable);
        List<Object> selectBindParams = newArrayList();
        appendFilter(requestMap, selectSql, selectBindParams);

        StringBuilder updateSql = new StringBuilder();
        List<Object> updateBindParams = newArrayList();
        appendTable(renderedTable, updateSql);
        appendSet(updateRow, updateSql, updateBindParams);
        if (filterSet.isEmpty()) {
            return new UpdateOrDeleteSingleCommandRenderResult(selectSql.toString(), selectBindParams, updateSql.toString(), updateBindParams);
        }

        appendFilter(requestMap, updateSql, updateBindParams);
        // 这里没有添加 limit 1

        return new UpdateOrDeleteSingleCommandRenderResult(selectSql.toString(), selectBindParams, updateSql.toString(), updateBindParams);
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以返回 true，表示在执行 SQL 之前需要使用原始 SQL 渲染命令。
     */
    @Override
    public boolean isRenderWithRawSql() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以返回 PostgreSQL 特定的转义 SQL 功能。
     */
    @Override
    public EscapeSql escapeStrFunc() {
        return POSTGRES_SQL_STR_ESCAPE;
    }
}

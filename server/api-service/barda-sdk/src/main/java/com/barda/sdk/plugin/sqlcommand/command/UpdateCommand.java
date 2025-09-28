package com.barda.sdk.plugin.sqlcommand.command;

import static com.google.common.collect.Lists.newArrayList;
import static com.barda.sdk.exception.PluginCommonError.INVALID_UPDATE_COMMAND;
import static com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet.parseChangeSet;
import static com.barda.sdk.plugin.sqlcommand.filter.FilterSet.parseFilterSet;
import static com.barda.sdk.util.MustacheHelper.renderMustacheString;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetRow;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;

/**
 * 更新命令类，实现了 {@link GuiSqlCommand} 接口。
 * 该类提供了一个通用的框架来执行 SQL 更新操作。
 */
@SuppressWarnings("DuplicatedCode")
public class UpdateCommand implements GuiSqlCommand {

    /**
     * 要在其中执行更新操作的表的名称。
     */
    protected final String table;

    /**
     * 更新操作的数据集合。
     */
    protected final ChangeSet changeSet;

    /**
     * 过滤器集合，用于指定更新操作的条件。
     */
    protected final FilterSet filterSet;

    /**
     * 一个布尔值，表示是否允许执行多次修改操作。
     * 如果为 true，则表示允许执行多次修改操作；
     * 如果为 false，则表示只执行一次修改操作。
     */
    protected final boolean allowMultiModify;

    /**
     * 列名前缀分隔符。
     */
    private final String columnFrontDelimiter;

    /**
     * 列名后缀分隔符。
     */
    private final String columnBackDelimiter;

    /**
     * 构造函数，使用提供的表名、数据集合、过滤器集合、是否允许多次修改操作、列分隔符来初始化 UpdateCommand 实例。
     */
    protected UpdateCommand(String table, ChangeSet changeSet,
            FilterSet filterSet, boolean allowMultiModify,
            String columnFrontDelimiter, String columnBackDelimiter) {
        this.table = table;
        this.changeSet = changeSet;
        this.filterSet = filterSet;
        this.allowMultiModify = allowMultiModify;
        this.columnFrontDelimiter = columnFrontDelimiter;
        this.columnBackDelimiter = columnBackDelimiter;
    }

    /**
     * 构造函数，使用提供的命令详细信息、列分隔符来初始化 UpdateCommand 实例。
     */
    protected UpdateCommand(Map<String, Object> commandDetail, String columnFrontDelimiter, String columnBackDelimiter) {
        this(GuiSqlCommand.parseTable(commandDetail),
                parseChangeSet(commandDetail),
                parseFilterSet(commandDetail),
                GuiSqlCommand.parseAllowMultiModify(commandDetail),
                columnFrontDelimiter,
                columnBackDelimiter);
    }

    /**
     * {@inheritDoc}
     * 渲染 SQL 更新命令并返回渲染结果。
     * 该方法根据是否使用原始 SQL 进行渲染来调用不同的渲染方法。
     */
    @Override
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {

        String renderedTable = renderMustacheString(table, requestMap);
        ChangeSetRow updateRow = changeSet.render(requestMap);
        if (updateRow.isEmpty()) {
            throw new PluginException(INVALID_UPDATE_COMMAND, "UPDATE_DATA_EMPTY");
        }

        StringBuilder sb = new StringBuilder();
        List<Object> bindParams = newArrayList();

        appendTable(renderedTable, sb);
        appendSet(updateRow, sb, bindParams);

        if (filterSet.isEmpty()) {
            return new GuiSqlCommandRenderResult(sb.toString(), bindParams);
        }

        appendFilter(requestMap, sb, bindParams);
        appendLimit(sb);

        return new GuiSqlCommandRenderResult(sb.toString(), bindParams);
    }

    /**
     * 追加 SET 部分到 SQL 语句中。
     * 该方法根据是否使用原始 SQL 进行追加。
     */
    protected void appendSet(ChangeSetRow updateRow, StringBuilder sb, List<Object> bindParams) {

        if (isRenderWithRawSql()) {
            sb.append(" set ");
            updateRow
                    .forEach(item -> sb.append(columnFrontDelimiter)
                            .append(item.column())
                            .append(columnBackDelimiter)
                            .append("=")
                            .append(item.guiSqlValue().getConcatSqlStr(escapeStrFunc()))
                            .append(",")
                    );
            sb.deleteCharAt(sb.length() - 1);
            return;
        }

        List<Object> setValueParams = updateRow.stream()
                .map(it -> it.guiSqlValue().getValue())
                .toList();
        bindParams.addAll(setValueParams);

        sb.append(" set ");
        updateRow.getColumns()
                .forEach(column -> sb.append(columnFrontDelimiter)
                        .append(column)
                        .append(columnBackDelimiter)
                        .append("=?,")
                );
        sb.deleteCharAt(sb.length() - 1);
    }

    /**
     * 追加表名到 SQL 语句中。
     */
    protected void appendTable(String renderedTable, StringBuilder sb) {
        sb.append("update ").append(renderedTable);
    }

    /**
     * 追加过滤器部分到 SQL 语句中。
     */
    protected void appendFilter(Map<String, Object> requestMap, StringBuilder sb, List<Object> bindParams) {
        GuiSqlCommandRenderResult render = filterSet.render(requestMap,
                columnFrontDelimiter, columnBackDelimiter, isRenderWithRawSql(), escapeStrFunc());
        sb.append(render.sql());
        bindParams.addAll(render.bindParams());
    }

    /**
     * 追加 LIMIT 部分到 SQL 语句中。
     */
    protected void appendLimit(StringBuilder sb) {
        if (!allowMultiModify) {
            sb.append(" limit 1");
        }
    }

    /**
     * {@inheritDoc}
     * 该方法返回 false，表示这不是一个插入命令。
     */
    @Override
    public boolean isInsertCommand() {
        return false;
    }

    /**
     * {@inheritDoc}
     * 从数据集合和过滤器集合中提取 Mustache 键并返回。
     */
    @Override
    public Set<String> extractMustacheKeys() {
        return Sets.union(filterSet.extractMustacheKeys(), changeSet.extractMustacheKeys());
    }
}

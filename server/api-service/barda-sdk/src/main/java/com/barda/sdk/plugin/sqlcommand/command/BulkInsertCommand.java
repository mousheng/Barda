package com.barda.sdk.plugin.sqlcommand.command;

import static com.google.common.collect.Lists.newArrayList;
import static com.barda.sdk.exception.PluginCommonError.INVALID_INSERT_COMMAND;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetItem;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetRow;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetRows;
import com.barda.sdk.util.MustacheHelper;

/**
 * 一个用于执行批量 INSERT 命令的类。
 * 它实现了 {@link GuiSqlCommand} 接口，并提供通用功能来执行批量 INSERT。
 */
public class BulkInsertCommand implements GuiSqlCommand {

    /**
     * 要插入的表名。
     */
    protected final String table;

    /**
     * 包含要插入的数据的 {@link BulkObjectChangeSet}。
     */
    protected final BulkObjectChangeSet bulkObjectChangeSet;

    /**
     * 列名前缀分隔符。
     */
    private final String columnFrontDelimiter;

    /**
     * 列名后缀分隔符。
     */
    private final String columnBackDelimiter;

    /**
     * 私有构造函数，用于从表名、{@link BulkObjectChangeSet}、列分隔符创建 {@link BulkInsertCommand} 实例。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含要插入的数据的 BulkObjectChangeSet
     * @param columnFrontDelimiter 列名前缀分隔符
     * @param columnBackDelimiter 列名后缀分隔符
     */
    protected BulkInsertCommand(String table, BulkObjectChangeSet bulkObjectChangeSet,
            String columnFrontDelimiter, String columnBackDelimiter) {
        this.table = table;
        this.bulkObjectChangeSet = bulkObjectChangeSet;
        this.columnFrontDelimiter = columnFrontDelimiter;
        this.columnBackDelimiter = columnBackDelimiter;
    }

    /**
     * 构造函数，用于从表名、{@link BulkObjectChangeSet} 和列分隔符创建 {@link BulkInsertCommand} 实例。
     * 列分隔符将用于前缀和后缀。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含要插入的数据的 BulkObjectChangeSet
     * @param columnDelimiter 列分隔符
     */
    protected BulkInsertCommand(String table, BulkObjectChangeSet bulkObjectChangeSet,
            String columnDelimiter) {
        this(table, bulkObjectChangeSet, columnDelimiter, columnDelimiter);
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以提供通用 SQL 渲染。
     * 它将表名和 {@link BulkObjectChangeSet} 渲染为 SQL 并返回 {@link GuiSqlCommandRenderResult}。
     */
    @SuppressWarnings("DuplicatedCode")
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {
        String renderedTable = MustacheHelper.renderMustacheString(table, requestMap);
        ChangeSetRows insertRows = bulkObjectChangeSet.render(requestMap);
        if (insertRows.isEmpty()) {
            throw new PluginException(INVALID_INSERT_COMMAND, "INSERT_DATA_EMPTY");
        }
        if (!insertRows.checkRowColumnAligned()) {
            throw new PluginException(INVALID_INSERT_COMMAND, "INVALID_INSERT_DATA");
        }

        StringBuilder sb = new StringBuilder();
        List<Object> bindParams = newArrayList();

        sb.append("insert into ")
                .append(renderedTable)
                .append(" (");

        Set<String> columns = insertRows.getColumns();
        columns.forEach(column ->
                sb.append(columnFrontDelimiter).append(column).append(columnBackDelimiter).append(",")
        );

        sb.deleteCharAt(sb.length() - 1).append(") values ");

        if (isRenderWithRawSql()) {
            for (ChangeSetRow row : insertRows) {
                sb.append("(");
                for (String column : columns) {
                    ChangeSetItem item = row.getItem(column);
                    sb.append(item.guiSqlValue().getConcatSqlStr(escapeStrFunc())).append(",");
                }
                sb.deleteCharAt(sb.length() - 1).append("),");
            }
        } else {
            for (ChangeSetRow row : insertRows) {
                appendQuestionMarks(sb, columns);
                for (String column : columns) {
                    ChangeSetItem item = row.getItem(column);
                    bindParams.add(item.guiSqlValue().getValue());
                }
            }
        }
        sb.deleteCharAt(sb.length() - 1);

        return new GuiSqlCommandRenderResult(sb.toString(), bindParams);
    }

    /**
     * 向 StringBuilder 追加一组问号，表示一行数据。
     *
     * @param sb 要追加的 StringBuilder
     * @param columns 列名集合
     */
    private static void appendQuestionMarks(StringBuilder sb, Set<String> columns) {
        sb.append("(")
                .append(Joiner.on(",").join(Collections.nCopies(columns.size(), "?")))
                .append("),");
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以返回 true，表示这是 INSERT 命令。
     */
    @Override
    public boolean isInsertCommand() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以从命令中提取 Mustache 键。
     */
    @Override
    public Set<String> extractMustacheKeys() {
        return bulkObjectChangeSet.extractMustacheKeys();
    }
}

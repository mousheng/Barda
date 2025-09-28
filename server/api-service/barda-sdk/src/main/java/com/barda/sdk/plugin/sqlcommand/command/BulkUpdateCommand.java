package com.barda.sdk.plugin.sqlcommand.command;

import static com.google.common.collect.Lists.newArrayList;
import static com.barda.sdk.exception.PluginCommonError.INVALID_INSERT_COMMAND;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetRows;
import com.barda.sdk.util.MustacheHelper;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue;

/**
 * 一个用于执行批量 UPDATE 命令的类。
 * 它实现了 {@link GuiSqlCommand} 接口，并提供通用功能来执行批量 UPDATE。
 */
public class BulkUpdateCommand implements GuiSqlCommand {

    /**
     * 要更新的表名。
     */
    protected final String table;

    /**
     * 包含要更新的数据的 {@link BulkObjectChangeSet}。
     */
    protected final BulkObjectChangeSet bulkObjectChangeSet;

    /**
     * 主键列名。
     */
    protected final String primaryKey;

    /**
     * 列名前缀分隔符。
     */
    protected final String columnFrontDelimiter;

    /**
     * 列名后缀分隔符。
     */
    protected final String columnBackDelimiter;

    /**
     * 私有构造函数，用于从表名、{@link BulkObjectChangeSet}、主键、列分隔符创建 {@link BulkUpdateCommand} 实例。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含要更新的数据的 BulkObjectChangeSet
     * @param primaryKey 主键列名
     * @param columnFrontDelimiter 列名前缀分隔符
     * @param columnBackDelimiter 列名后缀分隔符
     */
    protected BulkUpdateCommand(String table, BulkObjectChangeSet bulkObjectChangeSet, String primaryKey,
            String columnFrontDelimiter, String columnBackDelimiter) {
        this.table = table;
        this.bulkObjectChangeSet = bulkObjectChangeSet;
        this.primaryKey = primaryKey;
        this.columnFrontDelimiter = columnFrontDelimiter;
        this.columnBackDelimiter = columnBackDelimiter;
    }

    /**
     * 构造函数，用于从表名、{@link BulkObjectChangeSet}、主键和列分隔符创建 {@link BulkUpdateCommand} 实例。
     * 列分隔符将用于前缀和后缀。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含要更新的数据的 BulkObjectChangeSet
     * @param primaryKey 主键列名
     * @param columnDelimiter 列分隔符
     */
    protected BulkUpdateCommand(String table, BulkObjectChangeSet bulkObjectChangeSet, String primaryKey,
            String columnDelimiter) {
        this(table, bulkObjectChangeSet, primaryKey, columnDelimiter, columnDelimiter);
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以提供通用 SQL 渲染。
     * 它将表名和 {@link BulkObjectChangeSet} 渲染为 SQL 并返回 {@link GuiSqlCommandRenderResult}。
     */
    @Override
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {

        String renderedTable = MustacheHelper.renderMustacheString(table, requestMap);

        ChangeSetRows updateRows = bulkObjectChangeSet.render(requestMap);
        if (updateRows.isEmpty()) {
            throw new PluginException(INVALID_INSERT_COMMAND, "UPDATE_DATA_EMPTY");
        }

        if (updateRows.stream()
                .anyMatch(row -> !row.getColumns().contains(primaryKey))) {
            throw new PluginException(INVALID_INSERT_COMMAND, "BULK_UPDATE_DATA_NOT_CONTAIN_PRIMARY_KEY");
        }

        StringBuilder sb = new StringBuilder();
        List<Object> bindParams = newArrayList();

        sb.append("UPDATE ").append(renderedTable).append(" set\n");
        appendCaseWhen(updateRows, sb, bindParams);
        appendWhere(updateRows, sb, bindParams);

        return new GuiSqlCommandRenderResult(sb.toString(), bindParams);
    }

    /**
     * 向 StringBuilder 追加 WHERE 子句。
     *
     * @param updateRows 要更新的数据
     * @param sb 要追加的 StringBuilder
     * @param bindParams 绑定参数列表
     */
    private void appendWhere(ChangeSetRows updateRows, StringBuilder sb, List<Object> bindParams) {
        if (isRenderWithRawSql()) {
            String pkStr = updateRows.stream()
                    .map(row -> row.getItem(primaryKey).guiSqlValue().getConcatSqlStr(escapeStrFunc()))
                    .collect(Collectors.joining(","));
            sb.append("where ").append(primaryKey)
                    .append(" in (")
                    .append(pkStr)
                    .append(")");
            return;
        }

        String questionMarks = Joiner.on(",").join(Collections.nCopies(updateRows.size(), "?"));
        sb.append("where ").append(primaryKey)
                .append(" in (")
                .append(questionMarks)
                .append(")");
        bindParams.addAll(updateRows.stream()
                .map(row -> row.getItem(primaryKey).guiSqlValue().getValue())
                .toList());
    }

    /**
     * 向 StringBuilder 追加 CASE WHEN 子句。
     *
     * @param updateRows 要更新的数据
     * @param sb 要追加的 StringBuilder
     * @param bindParams 绑定参数列表
     */
    private void appendCaseWhen(ChangeSetRows updateRows, StringBuilder sb, List<Object> bindParams) {
        // column_1 = CASE WHEN any_column = value THEN column_1_value end,
        ArrayListMultimap<String, Pair<Object, Object>> columnToIdAndValue = ArrayListMultimap.create();

        updateRows.stream().forEach(row -> {
            Object pkValue = row.getItem(primaryKey).guiSqlValue().getValue();
                    row.stream()
                            .filter(changeSetItem -> !primaryKey.equals(changeSetItem.column()))
                            .forEach(changeSetItem -> {
                                String column = changeSetItem.column();
                                Object value = changeSetItem.guiSqlValue().getValue();
                                columnToIdAndValue.put(column, Pair.of(pkValue, value));
                            });
                }
        );
        columnToIdAndValue.asMap().forEach((column, pkAndValues) -> {
                    String columnWithDelimiter = columnFrontDelimiter + column + columnBackDelimiter;
                    sb.append(columnWithDelimiter)
                            .append(" = CASE ");
                    pkAndValues.forEach(pkAndValue -> {
                        Object pkValue = pkAndValue.getKey();
                        Object updateValue = pkAndValue.getValue();

                        if (isRenderWithRawSql()) {
                            sb.append("WHEN ")
                                    .append(columnFrontDelimiter)
                                    .append(primaryKey)
                                    .append(columnBackDelimiter)
                                    .append(" = ")
                                    .append(GuiSqlValue.from(pkValue).getConcatSqlStr(escapeStrFunc()))
                                    .append(" THEN ")
                                    .append(GuiSqlValue.from(updateValue).getConcatSqlStr(escapeStrFunc()))
                                    .append(" ");
                        } else {
                            sb.append("WHEN ")
                                    .append(columnFrontDelimiter)
                                    .append(primaryKey)
                                    .append(columnBackDelimiter)
                                    .append(" = ? THEN ? ");
                            bindParams.add(pkValue);
                            bindParams.add(updateValue);
                        }
                    });
                    sb.append("ELSE ").append(columnWithDelimiter).append(" END,\n");
                }
        );
        sb.deleteCharAt(sb.length() - 1)
                .deleteCharAt(sb.length() - 1)
                .append("\n");
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以返回 false，表示这是 UPDATE 命令，而不是 INSERT 命令。
     */
    @Override
    public boolean isInsertCommand() {
        return false;
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

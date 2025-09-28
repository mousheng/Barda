package com.barda.sdk.plugin.sqlcommand.command;

import static com.barda.sdk.exception.PluginCommonError.INVALID_INSERT_COMMAND;
import static com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet.parseChangeSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

import com.barda.sdk.exception.PluginException;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetItem;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetRow;
import com.barda.sdk.util.MustacheHelper;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue;

/**
 * 插入命令的抽象类，实现了 {@link GuiSqlCommand} 接口。
 * 该类提供了一个通用的框架来执行 SQL 插入操作，并可以根据需要进行子类化。
 */
public abstract class InsertCommand implements GuiSqlCommand {

    /**
     * 要在其中执行插入操作的表的名称。
     */
    private final String table;

    /**
     * 插入操作的数据集合。
     */
    private final ChangeSet changeSet;

    /**
     * 列名前缀分隔符。
     */
    private final String columnFrontDelimiter;

    /**
     * 列名后缀分隔符。
     */
    private final String columnBackDelimiter;

    /**
     * 构造函数，使用提供的命令详细信息、列分隔符来初始化 InsertCommand 实例。
     * 该构造函数使用相同的前缀和后缀分隔符。
     */
    protected InsertCommand(Map<String, Object> commandDetail, String columnDelimiter) {
        this(GuiSqlCommand.parseTable(commandDetail), parseChangeSet(commandDetail), columnDelimiter, columnDelimiter);
    }

    /**
     * 构造函数，使用提供的命令详细信息、列分隔符来初始化 InsertCommand 实例。
     */
    protected InsertCommand(Map<String, Object> commandDetail, String columnFrontDelimiter, String columnBackDelimiter) {
        this(GuiSqlCommand.parseTable(commandDetail), parseChangeSet(commandDetail), columnFrontDelimiter, columnBackDelimiter);
    }

    /**
     * 构造函数，使用提供的表名、数据集合、列分隔符来初始化 InsertCommand 实例。
     */
    protected InsertCommand(String table, ChangeSet changeSet, String columnFrontDelimiter, String columnBackDelimiter) {
        this.table = table;
        this.changeSet = changeSet;
        this.columnFrontDelimiter = columnFrontDelimiter;
        this.columnBackDelimiter = columnBackDelimiter;
    }

    /**
     * {@inheritDoc}
     * 渲染 SQL 插入命令并返回渲染结果。
     * 该方法根据是否使用原始 SQL 进行渲染来调用不同的渲染方法。
     */
    public GuiSqlCommandRenderResult render(Map<String, Object> requestParamMap) {
        String renderedTable = MustacheHelper.renderMustacheString(table, requestParamMap);
        ChangeSetRow insertRow = changeSet.render(requestParamMap);
        if (insertRow.isEmpty()) {
            throw new PluginException(INVALID_INSERT_COMMAND, "INSERT_DATA_EMPTY");
        }

        if (isRenderWithRawSql()) {
            return renderWithRawSql(renderedTable, insertRow);
        }
        return renderWithPreparedStatement(renderedTable, insertRow);
    }

    /**
     * 使用原始 SQL 渲染方法。
     */
    @Nonnull
    private GuiSqlCommandRenderResult renderWithRawSql(String table, ChangeSetRow insertRow) {
        return new GuiSqlCommandRenderResult(buildRawSql(table, insertRow), Collections.emptyList());
    }

    /**
     * 构建原始 SQL 插入语句。
     */
    private String buildRawSql(String table, ChangeSetRow insertRow) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ")
                .append(table)
                .append(" (");
        for (ChangeSetItem item : insertRow) {
            String column = item.column();
            sb.append(columnFrontDelimiter).append(column).append(columnBackDelimiter).append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append(") values (");

        for (ChangeSetItem item : insertRow) {
            GuiSqlValue guiSqlValue = item.guiSqlValue();
            sb.append(guiSqlValue.getConcatSqlStr(escapeStrFunc())).append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append(");");
        return sb.toString();
    }

    /**
     * 使用 PreparedStatement 渲染方法。
     */
    @Nonnull
    private GuiSqlCommandRenderResult renderWithPreparedStatement(String table, ChangeSetRow insertRow) {
        String sql = buildPsSql(table, insertRow);
        List<Object> bindParams = insertRow.stream()
                .map(item -> item.guiSqlValue().getValue())
                .toList();
        return new GuiSqlCommandRenderResult(sql, bindParams);
    }

    /**
     * 构建 PreparedStatement 插入语句。
     */
    @Nonnull
    private String buildPsSql(String table, ChangeSetRow insertRow) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ")
                .append(table)
                .append(" (");
        for (ChangeSetItem item : insertRow) {
            String column = item.column();
            sb.append(columnFrontDelimiter).append(column).append(columnBackDelimiter).append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append(") values (");
        String repeatedQuestionMarks = StringUtils.repeat("?,", insertRow.size());
        sb.append(repeatedQuestionMarks);
        sb.deleteCharAt(sb.length() - 1).append(")");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     * 该方法返回 true，表示这是一个插入命令。
     */
    @Override
    public boolean isInsertCommand() {
        return true;
    }

    /**
     * {@inheritDoc}
     * 从数据集合中提取 Mustache 键并返回。
     */
    @Override
    public Set<String> extractMustacheKeys() {
        return changeSet.extractMustacheKeys();
    }
}

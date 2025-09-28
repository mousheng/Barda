package com.barda.sdk.plugin.sqlcommand.command;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetItem;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetRow;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;

/**
 * 插入或更新命令的抽象类，实现了 {@link GuiSqlCommand} 接口。
 * 该类提供了一个通用的框架来执行 SQL 插入或更新操作。
 */
public abstract class UpsertCommand implements GuiSqlCommand {

    /**
     * 要在其中执行插入或更新操作的表的名称。
     */
    protected final String table;

    /**
     * 插入操作的数据集合。
     */
    protected final ChangeSet insertChangeSet;

    /**
     * 更新操作的数据集合。
     */
    protected final ChangeSet updateChangeSet;

    /**
     * 过滤器集合，用于指定插入或更新操作的条件。
     */
    protected final FilterSet filterSet;

    /**
     * 列名前缀分隔符。
     */
    protected final String columnFrontDelimiter;

    /**
     * 列名后缀分隔符。
     */
    protected final String columnBackDelimiter;

    /**
     * 构造函数，使用提供的表名、插入数据集合、更新数据集合、过滤器集合、列分隔符来初始化 UpsertCommand 实例。
     */
    protected UpsertCommand(String table, ChangeSet insertChangeSet,
            ChangeSet updateChangeSet,
            FilterSet filterSet,
            String columnFrontDelimiter,
            String columnBackDelimiter) {
        this.table = table;
        this.insertChangeSet = insertChangeSet;
        this.updateChangeSet = updateChangeSet;
        this.filterSet = filterSet;
        this.columnFrontDelimiter = columnFrontDelimiter;
        this.columnBackDelimiter = columnBackDelimiter;
    }

    /**
     * 追加 UPDATE 部分到 SQL 语句中。
     */
    protected void appendUpdateValues(ChangeSetRow updateRow, StringBuilder sb, List<Object> bindParams) {
        for (ChangeSetItem item : updateRow) {
            String column = item.column();
            sb.append(columnFrontDelimiter)
                    .append(column)
                    .append(columnBackDelimiter)
                    .append("=?,");
            bindParams.add(item.guiSqlValue().getValue());
        }
        sb.deleteCharAt(sb.length() - 1);
    }

    /**
     * 追加 ON DUPLICATE KEY UPDATE 关键字到 SQL 语句中。
     */
    protected void appendUpsertKeyword(StringBuilder sb) {
        sb.append(" on duplicate key update ");
    }

    /**
     * 追加 INSERT 部分到 SQL 语句中。
     */
    protected void appendInsertValues(ChangeSetRow insertRow, StringBuilder sb, List<Object> bindParams) {
        sb.append(" (");
        for (String column : insertRow.getColumns()) {
            sb.append(columnFrontDelimiter)
                    .append(column)
                    .append(columnBackDelimiter)
                    .append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append(") values (");
        for (ChangeSetItem item : insertRow) {
            Object value = item.guiSqlValue().getValue();
            sb.append("?,");
            bindParams.add(value);
        }
        sb.deleteCharAt(sb.length() - 1).append(")");
    }

    /**
     * 追加表名到 SQL 语句中。
     */
    protected void appendTable(String renderedTable, StringBuilder sb, boolean updateChangeEmpty) {
        if (updateChangeEmpty) {
            sb.append("insert ignore into ").append(renderedTable);
        } else {
            sb.append("insert into ").append(renderedTable);
        }
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
     * 从数据集合和过滤器集合中提取 Mustache 键并返回。
     */
    @Override
    public Set<String> extractMustacheKeys() {
        return Sets.union(filterSet.extractMustacheKeys(),
                Sets.union(insertChangeSet.extractMustacheKeys(), updateChangeSet.extractMustacheKeys()));
    }
}

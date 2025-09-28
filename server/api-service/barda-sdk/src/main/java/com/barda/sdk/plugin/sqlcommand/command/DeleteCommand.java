package com.barda.sdk.plugin.sqlcommand.command;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;
import com.barda.sdk.util.MustacheHelper;

/**
 * 实现了 {@link GuiSqlCommand} 接口的 DeleteCommand 类，用于执行 SQL 删除操作。
 */
public class DeleteCommand implements GuiSqlCommand {

    /**
     * 要从中执行删除操作的表的名称。
     */
    protected final String table;

    /**
     * 应用于删除操作的过滤器集合。
     */
    protected final FilterSet filterSet;

    /**
     * 指示是否允许对多行进行修改。
     */
    protected final boolean allowMultiModify;

    /**
     * 列名前缀分隔符。
     */
    protected final String columnFrontDelimiter;

    /**
     * 列名后缀分隔符。
     */
    protected final String columnBackDelimiter;

    /**
     * 构造函数，使用提供的表名、过滤器集合、是否允许多行修改、列分隔符来初始化 DeleteCommand 实例。
     */
    protected DeleteCommand(String table, FilterSet filterSet, boolean allowMultiModify,
            String columnFrontDelimiter, String columnBackDelimiter) {
        this.table = table;
        this.filterSet = filterSet;
        this.allowMultiModify = allowMultiModify;
        this.columnFrontDelimiter = columnFrontDelimiter;
        this.columnBackDelimiter = columnBackDelimiter;
    }

    /**
     * 构造函数，使用提供的表名、过滤器集合、是否允许多行修改、列分隔符来初始化 DeleteCommand 实例。
     * 该构造函数使用相同的前缀和后缀分隔符。
     */
    protected DeleteCommand(String table, FilterSet filterSet, boolean allowMultiModify,
            String columnDelimiter) {
        this(table, filterSet, allowMultiModify, columnDelimiter, columnDelimiter);
    }

    /**
     * {@inheritDoc}
     * 渲染 SQL 删除命令并返回渲染结果。
     */
    @Override
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {

        String renderedTable = MustacheHelper.renderMustacheString(table, requestMap);

        StringBuilder sb = new StringBuilder();
        renderTable(renderedTable, sb);
        if (filterSet.isEmpty()) {
            renderLimit(sb);
            return new GuiSqlCommandRenderResult(sb.toString(), Collections.emptyList());
        }

        GuiSqlCommandRenderResult render = filterSet.render(requestMap, columnFrontDelimiter, columnBackDelimiter, isRenderWithRawSql(),
                escapeStrFunc());
        sb.append(render.sql());
        renderLimit(sb);
        return new GuiSqlCommandRenderResult(sb.toString(), render.bindParams());
    }

    /**
     * 渲染表名并将其添加到 StringBuilder 中。
     */
    protected void renderTable(String renderedTable, StringBuilder sb) {
        sb.append("delete from ").append(renderedTable);
    }

    /**
     * 如果不允许多行修改，则在 StringBuilder 中添加 limit 1。
     */
    protected void renderLimit(StringBuilder sb) {
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
     * 从过滤器集合中提取 Mustache 键并返回。
     */
    @Override
    public Set<String> extractMustacheKeys() {
        return filterSet.extractMustacheKeys();
    }
}

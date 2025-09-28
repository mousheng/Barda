package com.barda.sdk.plugin.sqlcommand.filter;

import static com.google.common.collect.Lists.newArrayList;
import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.exception.PluginCommonError.INVALID_IN_OPERATOR_SETTINGS;
import static com.barda.sdk.util.JsonUtils.toJson;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ForwardingList;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand.GuiSqlCommandRenderResult;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet.FilterCondition;
import com.barda.sdk.util.MustacheHelper;
import com.barda.sdk.util.SqlGuiUtils;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

import lombok.Getter;

/**
 * 过滤器集合类，继承自 {@link ForwardingList} 并实现了 {@link FilterSet} 接口。
 * 该类提供了一个通用的框架来执行 SQL 过滤操作。
 */
public class FilterSet extends ForwardingList<FilterCondition> {

    /**
     * 存储过滤器条件的内部列表。
     */
    private final ArrayList<FilterCondition> filters = newArrayList();

    /**
     * 向过滤器集合中添加一个条件。
     *
     * @param column  列名。
     * @param condition  条件。
     * @param value  值。
     */
    public void addCondition(String column, String condition, Object value) {
        filters.add(new FilterCondition(column, condition, value));
    }

    /**
     * 向过滤器集合中添加一个 {@link FilterCondition} 对象。
     *
     * @param condition  过滤器条件。
     */
    public void addCondition(FilterCondition condition) {
        filters.add(condition);
    }

    /**
     * {@inheritDoc}
     * 获取内部列表的代理。
     */
    @Override
    protected List<FilterCondition> delegate() {
        return filters;
    }

    /**
     * 渲染过滤器集合并返回 SQL 语句和绑定参数。
     *
     * @param requestMap  请求参数。
     * @param columnFrontDelimiter  列名前缀分隔符。
     * @param columnBackDelimiter  列名后缀分隔符。
     * @param renderWithRawSql  是否使用原始 SQL 渲染。
     * @param escapeSql  SQL 转义函数。
     * @return 渲染后的 SQL 语句和绑定参数。
     */
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap,
            String columnFrontDelimiter, String columnBackDelimiter, boolean renderWithRawSql, EscapeSql escapeSql) {

        if (filters.isEmpty()) {
            return new GuiSqlCommandRenderResult("", emptyList());
        }
        StringBuilder sb = new StringBuilder(" where ");
        List<Object> bindParams = newArrayList();

        for (int i = 0; i < filters.size(); i++) {
            FilterCondition filterCondition = filters.get(i);
            String column = filterCondition.getColumn();
            Object value = filterCondition.getValue();
            String condition = filterCondition.getCondition();

            RenderItem renderItem;
            if (filterCondition instanceof RawFilterCondition it) {
                renderItem = RenderItem.withRawSql(it.getColumn() + it.getCondition() + it.getValue());
            } else {
                renderItem = renderCondition(condition, value, requestMap, column,
                        columnFrontDelimiter, columnBackDelimiter, renderWithRawSql, escapeSql);
            }
            sb.append(renderItem.conditionSql());
            if (renderItem.needBind()) {
                bindParams.add(renderItem.bindValue());
            }
            if (i != filters.size() - 1) {
                sb.append(" and ");
            }
        }

        return new GuiSqlCommandRenderResult(sb.toString(), bindParams);
    }

    /**
     * 渲染单个条件并返回 SQL 语句和绑定参数。
     *
     * @param condition  条件。
     * @param value  值。
     * @param requestMap  请求参数。
     * @param column  列名。
     * @param columnFrontDelimiter  列名前缀分隔符。
     * @param columnBackDelimiter  列名后缀分隔符。
     * @param renderWithRawSql  是否使用原始 SQL 渲染。
     * @param escapeSql  SQL 转义函数。
     * @return 渲染后的单个条件的 SQL 语句和绑定参数。
     */
    private RenderItem renderCondition(String condition, Object value, Map<String, Object> requestMap, String column,
            String columnFrontDelimiter, String columnBackDelimiter, boolean renderWithRawSql, EscapeSql escapeSql) {
        String columnWithDelimiter = columnFrontDelimiter + column + columnBackDelimiter;

        switch (condition) {
            case "=", "!=", ">", "<", "<=", ">=" -> {
                GuiSqlValue guiSqlValue = SqlGuiUtils.renderPsBindValue(value, requestMap);
                if (renderWithRawSql) {
                    return RenderItem.withRawSql(columnWithDelimiter + " " + condition + " " + guiSqlValue.getConcatSqlStr(escapeSql));
                }
                return RenderItem.withPs(columnWithDelimiter + " " + condition + " ? ", guiSqlValue.getValue());
            }
            case "IS", "IS NOT" -> {
                GuiSqlValue guiSqlValue = SqlGuiUtils.renderPsBindValue(value, requestMap);
                if (guiSqlValue.getValue() == null || guiSqlValue.getValue() instanceof Boolean) {
                    return RenderItem.withRawSql(columnWithDelimiter + " " + condition + " " + guiSqlValue.getValue() + " ");
                }
                throw new PluginException(INVALID_IN_OPERATOR_SETTINGS, "INVALID_IS");
            }
            case "IN", "NOT IN" -> {
                GuiSqlValue guiSqlValue = SqlGuiUtils.renderPsBindValue(value, requestMap);
                if (!(guiSqlValue.getRawValue() instanceof List<?> list)) {
                    throw new PluginException(INVALID_IN_OPERATOR_SETTINGS, "INVALID_IN");
                }
                if (list.isEmpty()) {
                    return RenderItem.withRawSql("false");
                }

                return RenderItem.withRawSql(columnWithDelimiter + " " + condition + getCollectionStr(list));
            }
            default -> throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_FILTER_FIELD", condition);
        }
    }

    /**
     * 获取集合的字符串表示形式。
     *
     * @param list  集合。
     * @return 集合的字符串表示形式。
     */
    private static String getCollectionStr(List<?> list) {
        String result = list.stream()
                .map(obj -> {
                    if (obj instanceof String) {
                        return "'" + obj + "'";
                    }
                    if (obj instanceof Collection<?> || obj instanceof Map<?, ?>) {
                        return toJson(obj);
                    }
                    return String.valueOf(obj);
                })
                .collect(Collectors.joining(","));
        return " (" + result + ")";
    }

    /**
     * 内部类，表示渲染后的单个条件的 SQL 语句和绑定参数。
     */
    private record RenderItem(String conditionSql, Object bindValue, boolean needBind) {

        public static RenderItem withPs(String conditionSql, Object bindValue) {
            return new RenderItem(conditionSql, bindValue, true);
        }

        public static RenderItem withRawSql(String conditionSql) {
            return new RenderItem(conditionSql, null, false);
        }
    }


    /**
     * 内部类，表示单个过滤器条件。
     */
    @Getter
    public static class FilterCondition {
        private final String column;
        private final String condition;
        private final Object value;

        /**
         * 构造函数。
         *
         * @param column  列名。
         * @param condition  条件。
         * @param value  值。
         */
        public FilterCondition(String column, String condition, Object value) {
            this.column = column;
            this.condition = condition;
            this.value = value;
        }
    }

    /**
     * 内部类，表示原始的过滤器条件。
     */
    public static class RawFilterCondition extends FilterCondition {
        public RawFilterCondition(String column, String condition, Object value) {
            super(column, condition, value);
        }
    }

    /**
     * 从命令详情中解析并创建 {@link FilterSet} 对象。
     *
     * @param commandDetail  命令详情。
     * @return 创建的 {@link FilterSet} 对象。
     */
    @SuppressWarnings("unchecked")
    public static FilterSet parseFilterSet(Map<String, Object> commandDetail) {
        Object filterBy = MapUtils.getObject(commandDetail, "filterBy", null);
        if (filterBy == null) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_FILTER_FIELD_EMPTY");
        }

        if (!(filterBy instanceof List<?> list)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_FILTER_FIELD", filterBy.getClass().getSimpleName());
        }

        FilterSet filterSet = new FilterSet();
        for (Object o : list) {
            if (!(o instanceof Map<?, ?>)) {
                throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_FILTER_FIELD", o.getClass().getSimpleName());
            }

            Map<String, Object> map = (Map<String, Object>) o;
            String column = MapUtils.getString(map, "column");
            String condition = MapUtils.getString(map, "condition");
            Object value = MapUtils.getObject(map, "value");

            if (StringUtils.isAnyBlank(column, condition)) {
                throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_FILTER_CONDITION");
            }

            filterSet.addCondition(column, condition.toUpperCase(), value);
        }
        return filterSet;
    }

    /**
     * 从过滤器集合中提取 Mustache 键。
     *
     * @return Mustache 键的集合。
     */
    public Set<String> extractMustacheKeys() {
        return stream()
                .filter(it -> it.getValue() instanceof String)
                .map(filterCondition -> MustacheHelper.extractMustacheKeysWithCurlyBraces(String.valueOf(filterCondition.getValue())))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
}

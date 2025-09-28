package com.barda.sdk.plugin.sqlcommand.changeset;

import static com.google.common.collect.Sets.newHashSet;
import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import com.barda.sdk.exception.PluginException;

/**
 * 表示变更集行的记录类，实现Iterable接口。
 */
public record ChangeSetRows(List<ChangeSetRow> rows) implements Iterable<ChangeSetRow> {

    /**
     * 从JsonNode对象中创建ChangeSetRows对象。
     *
     * @param node JsonNode对象
     * @return ChangeSetRows对象
     * @throws PluginException 如果JsonNode不是数组格式，则抛出异常
     */
    @SuppressWarnings("UnstableApiUsage")
    @Nonnull
    public static ChangeSetRows fromJsonNode(JsonNode node) {
        if (!(node instanceof ArrayNode arrayNode)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_ARRAY_FORMAT");
        }

        List<ChangeSetRow> changeSetRows = Streams.stream(arrayNode.iterator())
                .map(ChangeSetRow::new)
                .toList();
        return new ChangeSetRows(changeSetRows);
    }

    /**
     * 返回变更集行的迭代器。
     *
     * @return 变更集行的迭代器
     */
    @Nonnull
    @Override
    public Iterator<ChangeSetRow> iterator() {
        return rows.iterator();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public Stream<ChangeSetRow> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * 检查行列是否对齐。
     *
     * @return 如果行列对齐返回true，否则返回false
     */
    public boolean checkRowColumnAligned() {
        ChangeSetRow changeSetRow = rows.get(0);
        Set<String> columns = changeSetRow.getColumns();

        HashSet<String> columnSet = newHashSet(columns);

        for (ChangeSetRow row : this) {
            if (!match(columnSet, row.getColumns())) {
                return false;
            }
        }
        return true;
    }

    private boolean match(Set<String> columns, Set<String> otherColumns) {
        if (columns.size() != otherColumns.size()) {
            return false;
        }
        return columns.containsAll(otherColumns);
    }

    /**
     * 获取变更集行的数量。
     *
     * @return 变更集行的数量
     */
    public int size() {
        return rows.size();
    }

    /**
     * 获取变更集行的列。
     *
     * @return 变更集行的列集合
     */
    public Set<String> getColumns() {
        return rows.get(0).getColumns();
    }
}
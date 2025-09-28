package com.barda.sdk.plugin.sqlcommand.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue;

/**
 * 用于表示更改集行的类。
 * 该类实现了Iterable接口，可以迭代地获取ChangeSetItem。
 */
@SuppressWarnings("UnstableApiUsage")
public class ChangeSetRow implements Iterable<ChangeSetItem> {

    /**
     * 用于存储ChangeSetItem的Map。
     * 键为列名，值为ChangeSetItem。
     */
    private final Map<String, ChangeSetItem> columnToItem;

    /**
     * 私有构造函数，用于创建ChangeSetRow实例。
     *
     * @param items 包含ChangeSetItem的列表
     */
    public ChangeSetRow(List<ChangeSetItem> items) {
        columnToItem = items.stream()
                .collect(Collectors.toMap(ChangeSetItem::column, it -> it, (a, b) -> b, LinkedHashMap::new));
    }

    /**
     * 私有构造函数，用于创建ChangeSetRow实例。
     *
     * @param node 包含ChangeSetItem的原始JSON数据
     */
    public ChangeSetRow(JsonNode node) {
        this(parseChangeSetItems(node));
    }

    /**
     * 解析原始JSON数据并返回ChangeSetItem的列表。
     *
     * @param node 包含原始JSON数据的JSON节点
     * @return 解析出的ChangeSetItem的列表
     */
    @Nonnull
    private static List<ChangeSetItem> parseChangeSetItems(JsonNode node) {
        if (!(node instanceof ObjectNode objectNode)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_MAP_TYPE");
        }

        return Streams.stream(objectNode.fields())
                .map(next -> {
                    String column = next.getKey();
                    JsonNode value = next.getValue();
                    return new ChangeSetItem(column, GuiSqlValue.fromJsonNode(value));
                })
                .toList();
    }

    /**
     * 判断更改集行是否为空。
     *
     * @return 如果更改集行为空，返回true；否则返回false
     */
    public boolean isEmpty() {
        return columnToItem.isEmpty();
    }

    /**
     * 获取更改集行的流式视图。
     *
     * @return 更改集行的流式视图
     */
    public Stream<ChangeSetItem> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * 获取更改集行中ChangeSetItem的数量。
     *
     * @return 更改集行中ChangeSetItem的数量
     */
    public int size() {
        return columnToItem.size();
    }

    /**
     * 获取更改集行的迭代器。
     *
     * @return 更改集行的迭代器
     */
    @Nonnull
    @Override
    public Iterator<ChangeSetItem> iterator() {
        return columnToItem.values().iterator();
    }


    /**
     * 获取更改集行中包含的列名的集合。
     *
     * @return 更改集行中包含的列名的集合
     */
    public Set<String> getColumns() {
        return columnToItem.keySet();
    }

    /**
     * 获取指定列的ChangeSetItem。
     *
     * @param column 列名
     * @return 指定列的ChangeSetItem
     */
    public ChangeSetItem getItem(String column) {
        return columnToItem.get(column);
    }
}
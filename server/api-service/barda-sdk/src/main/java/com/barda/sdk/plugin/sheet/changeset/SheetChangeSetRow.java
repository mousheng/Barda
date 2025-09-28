package com.barda.sdk.plugin.sheet.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.JsonUtils;

/**
 * 用于表示SheetChangeSet中的一行的类。
 * 该类实现了Iterable接口，可以迭代地获取SheetChangeSetItem。
 */
public class SheetChangeSetRow implements Iterable<SheetChangeSetItem> {

    /**
     * 包含SheetChangeSetItem的列表。
     */
    private final List<SheetChangeSetItem> items;

    /**
     * 包含所有列名的集合。
     */
    private final Set<String> columns;

    /**
     * 用于将列名映射到SheetChangeSetItem的Map。
     */
    private final Map<String, SheetChangeSetItem> columnToItem;

    /**
     * 私有构造函数，用于创建SheetChangeSetRow实例。
     *
     * @param items 包含SheetChangeSetItem的列表
     */
    public SheetChangeSetRow(List<SheetChangeSetItem> items) {
        this.items = items;
        columns = items.stream()
                .map(SheetChangeSetItem::column)
                .collect(Collectors.toUnmodifiableSet());
        columnToItem = items.stream()
                .collect(Collectors.toMap(SheetChangeSetItem::column, it -> it, (a, b) -> b));
    }

    /**
     * 从JSON节点创建SheetChangeSetRow。
     *
     * @param node 包含SheetChangeSetItem的JSON节点
     * @return 创建的SheetChangeSetRow
     * @throws PluginException 如果JSON节点的类型不正确
     */
    @Nonnull
    public static SheetChangeSetRow fromJsonNode(JsonNode node) {
        if (!(node instanceof ObjectNode objectNode)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_MAP_TYPE");
        }
        List<SheetChangeSetItem> result = new ArrayList<>();
        Iterator<Entry<String, JsonNode>> iterator = objectNode.fields();
        while (iterator.hasNext()) {
            Entry<String, JsonNode> next = iterator.next();
            String column = next.getKey();
            JsonNode value = next.getValue();
            result.add(new SheetChangeSetItem(column, JsonUtils.jsonNodeToObject(value)));
        }
        return new SheetChangeSetRow(result);
    }

    /**
     * 判断SheetChangeSetRow是否为空。
     *
     * @return true表示为空，false表示不为空
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * 获取SheetChangeSetRow的迭代器。
     *
     * @return 迭代器
     */
    @Nonnull
    @Override
    public Iterator<SheetChangeSetItem> iterator() {
        return items.iterator();
    }

    /**
     * 获取包含所有列名的集合。
     *
     * @return 包含所有列名的集合
     */
    public Set<String> getColumns() {
        return columns;
    }

    /**
     * 获取指定列名对应的SheetChangeSetItem。
     *
     * @param column 列名
     * @return 指定列名对应的SheetChangeSetItem，如果不存在则返回null
     */
    public SheetChangeSetItem getItem(String column) {
        return columnToItem.get(column);
    }
}
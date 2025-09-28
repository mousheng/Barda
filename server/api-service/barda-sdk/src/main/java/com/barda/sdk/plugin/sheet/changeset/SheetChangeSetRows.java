package com.barda.sdk.plugin.sheet.changeset;


import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;
import com.barda.sdk.exception.PluginException;

/**
 * 用于表示SheetChangeSet中的多行的记录类。
 * 该类实现了Iterable接口，可以迭代地获取SheetChangeSetRow。
 */
public record SheetChangeSetRows(List<SheetChangeSetRow> rows) implements Iterable<SheetChangeSetRow> {

    /**
     * 从JSON节点创建SheetChangeSetRows。
     *
     * @param node 包含SheetChangeSetRow的JSON节点
     * @return 创建的SheetChangeSetRows
     * @throws PluginException 如果JSON节点的类型不正确
     */
    @SuppressWarnings("UnstableApiUsage")
    @Nonnull
    public static SheetChangeSetRows fromJsonNode(JsonNode node) {
        if (!(node instanceof ArrayNode arrayNode)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_ARRAY_FORMAT");
        }

        List<SheetChangeSetRow> changeSetRows = Streams.stream(arrayNode.iterator())
                .map(SheetChangeSetRow::fromJsonNode)
                .toList();
        return new SheetChangeSetRows(changeSetRows);
    }

    /**
     * 获取SheetChangeSetRows的迭代器。
     *
     * @return 迭代器
     */
    @Nonnull
    @Override
    public Iterator<SheetChangeSetRow> iterator() {
        return rows.iterator();
    }

    /**
     * 判断SheetChangeSetRows是否为空。
     *
     * @return true表示为空，false表示不为空
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
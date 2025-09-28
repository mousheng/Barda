package com.barda.sdk.plugin.sqlcommand.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.util.JsonUtils.toJson;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.MustacheHelper;
import com.barda.sdk.util.SqlGuiUtils;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue;

import lombok.extern.slf4j.Slf4j;

/**
 * 用于表示键值对更改集的类。
 * 该类继承自ChangeSet类，并实现了ChangeSet接口。
 */
@Slf4j
public class KeyValuePairChangeSet extends ChangeSet {

    /**
     * 用于存储键值对的Map。
     * 键为列名，值为Object类型。
     */
    private final Map<String, Object> columnValueMap;

    /**
     * 私有构造函数，用于创建KeyValuePairChangeSet实例。
     *
     * @param columnValueMap 包含键值对的Map
     */
    private KeyValuePairChangeSet(Map<String, Object> columnValueMap) {
        this.columnValueMap = columnValueMap;
    }

    /**
     * 构造函数，用于从Object类型的数据中解析出键值对并创建KeyValuePairChangeSet实例。
     *
     * @param comp 包含键值对的Object类型数据
     */
    public KeyValuePairChangeSet(Object comp) {
        this(parseColumnValueMap(comp));
    }

    /**
     * 解析原始Object类型的数据并返回包含键值对的Map。
     *
     * @param comp 包含原始Object类型数据的Object
     * @return 包含键值对的Map
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    private static Map<String, Object> parseColumnValueMap(Object comp) {
        if (!(comp instanceof List<?> list)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_PARAM", toJson(comp));
        }

        return list.stream()
                .map(o -> {
                    if (!(o instanceof Map<?, ?> map)) {
                        throw new PluginException(INVALID_GUI_SETTINGS, "GUI_CHANGE_SET_TYPE_ERROR", o.getClass().getSimpleName());
                    }

                    String column = MapUtils.getString((Map<String, ?>) map, "column");
                    if (StringUtils.isBlank(column)) {
                        throw new PluginException(INVALID_GUI_SETTINGS, "GUI_CHANGE_SET_FIELD_EMPTY");
                    }

                    Object value = MapUtils.getObject((Map<String, ?>) map, "value");
                    return Pair.of(column, value);
                })
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    /**
     * 用于测试的构建器方法，用于创建KeyValuePairChangeSet实例。
     *
     * @param kvMap 包含键值对的Map
     * @return 包含键值对的KeyValuePairChangeSet实例
     */
    @VisibleForTesting
    public static KeyValuePairChangeSet buildForTest(Map<String, Object> kvMap) {
        return new KeyValuePairChangeSet(kvMap);
    }

    /**
     * 将原始数据渲染为ChangeSetRow并返回。
     *
     * @param requestMap 包含渲染所需的数据的Map
     * @return 包含ChangeSetItem的ChangeSetRow
     */
    @Override
    public ChangeSetRow render(Map<String, Object> requestMap) {
        List<ChangeSetItem> result = new ArrayList<>();
        for (var entry : columnValueMap.entrySet()) {
            String column = entry.getKey();
            Object value = entry.getValue();
            GuiSqlValue guiSqlValue = SqlGuiUtils.renderPsBindValue(value, requestMap);
            result.add(new ChangeSetItem(column, guiSqlValue));
        }
        return new ChangeSetRow(result);
    }

    /**
     * 从原始数据中提取Mustache键并返回。
     *
     * @return 包含Mustache键的Set
     */
    @Override
    public Set<String> extractMustacheKeys() {
        return columnValueMap.values().stream()
                .filter(String.class::isInstance)
                .flatMap(o -> MustacheHelper.extractMustacheKeysWithCurlyBraces((String) o).stream())
                .collect(Collectors.toSet());
    }
}

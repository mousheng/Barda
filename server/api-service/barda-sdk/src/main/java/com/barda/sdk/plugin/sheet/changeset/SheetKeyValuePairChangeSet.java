package com.barda.sdk.plugin.sheet.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.util.MustacheHelper.renderMustacheJson;
import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 用于表示SheetKeyValuePairChangeSet的类。
 * 该类继承自SheetChangeSet类，并使用Lombok的@Slf4j注解来自动生成日志记录器。
 */
@Slf4j
public class SheetKeyValuePairChangeSet extends SheetChangeSet {

    /**
     * 用于存储键值对的Map。
     */
    private Map<String, String> map = emptyMap();

    /**
     * 私有构造函数，用于创建SheetKeyValuePairChangeSet实例。
     *
     * @param comp 包含键值对的原始数据
     */
    @SuppressWarnings("unchecked")
    public SheetKeyValuePairChangeSet(Object comp) {
        if (!(comp instanceof List<?> list)) {
            return;
        }
        map = list.stream()
                .map(o -> {
                    if (!(o instanceof Map<?, ?> map)) {
                        throw new PluginException(INVALID_GUI_SETTINGS, "GUI_CHANGE_SET_TYPE_ERROR", o.getClass().getSimpleName());
                    }
                    String column = MapUtils.getString((Map<String, ?>) map, "column");
                    if (StringUtils.isBlank(column)) {
                        throw new PluginException(INVALID_GUI_SETTINGS, "GUI_CHANGE_SET_FIELD_EMPTY");
                    }
                    String value = MapUtils.getString((Map<String, ?>) map, "value");
                    return Pair.of(column, value);
                })
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue, (a, b) -> b));
    }

    /**
     * 重写render方法，用于将原始数据渲染为SheetChangeSetRow。
     *
     * @param requestMap 包含渲染所需的数据的Map
     * @return 渲染后的SheetChangeSetRow
     */
    @Override
    public SheetChangeSetRow render(Map<String, Object> requestMap) {
        List<SheetChangeSetItem> result = new ArrayList<>();
        for (String column : map.keySet()) {
            Object renderedValue = JsonUtils.jsonNodeToObject(renderMustacheJson(map.get(column), requestMap));
            result.add(new SheetChangeSetItem(column, renderedValue));
        }
        return new SheetChangeSetRow(result);
    }
}
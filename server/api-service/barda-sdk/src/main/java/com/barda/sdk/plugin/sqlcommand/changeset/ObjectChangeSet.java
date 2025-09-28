package com.barda.sdk.plugin.sqlcommand.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.MustacheHelper;

/**
 * 用于表示对象更改集的类。
 * 该类继承自ChangeSet类，并实现了ChangeSet接口。
 */
public class ObjectChangeSet extends ChangeSet {

    /**
     * 包含原始JSON字符串的成员变量。
     */
    private final String str;

    /**
     * 私有构造函数，用于创建ObjectChangeSet实例。
     *
     * @param str 包含原始JSON字符串
     */
    public ObjectChangeSet(String str) {
        this.str = str;
    }

    /**
     * 将原始数据渲染为ChangeSetRow并返回。
     *
     * @param requestMap 包含渲染所需的数据的Map
     * @return 包含ChangeSetItem的ChangeSetRow
     */
    @Override
    public ChangeSetRow render(Map<String, Object> requestMap) {
        JsonNode jsonNode;
        try {
            jsonNode = MustacheHelper.renderMustacheJson(str, requestMap);
        } catch (Throwable e) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_MAP_TYPE");
        }

        return new ChangeSetRow(jsonNode);
    }

    /**
     * 从原始数据中提取Mustache键并返回。
     *
     * @return 包含Mustache键的Set
     */
    @Override
    public Set<String> extractMustacheKeys() {
        return MustacheHelper.extractMustacheKeysWithCurlyBraces(str);
    }
}

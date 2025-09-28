package com.barda.sdk.plugin.sheet.changeset;


import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.plugin.sheet.changeset.SheetChangeSetRow.fromJsonNode;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.MustacheHelper;

/**
 * 表示Sheet对象变更集的类，继承自SheetChangeSet。
 */
public class SheetObjectChangeSet extends SheetChangeSet {

    /**
     * 字符串表示的变更集
     */
    private final String str;

    /**
     * 构造一个新的Sheet对象变更集对象。
     *
     * @param str 字符串表示的变更集
     */
    public SheetObjectChangeSet(String str) {
        this.str = str;
    }

    /**
     * 将请求映射渲染成Sheet变更集行。
     *
     * @param requestMap 请求映射
     * @return Sheet变更集行对象
     * @throws PluginException 如果无法渲染变更集，则抛出异常
     */
    @Override
    public SheetChangeSetRow render(Map<String, Object> requestMap) {
        JsonNode jsonNode;
        try {
            jsonNode = MustacheHelper.renderMustacheJson(str, requestMap);
        } catch (Throwable e) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_MAP_TYPE");
        }
        return fromJsonNode(jsonNode);
    }
}

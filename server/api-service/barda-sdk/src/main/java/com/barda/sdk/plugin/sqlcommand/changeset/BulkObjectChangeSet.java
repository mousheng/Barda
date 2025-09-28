package com.barda.sdk.plugin.sqlcommand.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.plugin.common.constant.Constants.PRIMARY_KEY_FORM_KEY;
import static com.barda.sdk.plugin.common.constant.Constants.RECORD_FORM_KEY;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.MustacheHelper;

/**
 * 用于处理批量对象更改集的类。
 */
public class BulkObjectChangeSet {

    /**
     * 包含原始数据的字符串。
     */
    private final String str;

    /**
     * 私有构造函数，用于创建BulkObjectChangeSet实例。
     *
     * @param str 包含原始数据的字符串
     */
    public BulkObjectChangeSet(String str) {
        this.str = str;
    }

    /**
     * 将原始数据渲染为ChangeSetRows。
     *
     * @param requestMap 包含渲染所需的数据的Map
     * @return 渲染后的ChangeSetRows
     */
    public ChangeSetRows render(Map<String, Object> requestMap) {

        JsonNode jsonNode;
        try {
            jsonNode = MustacheHelper.renderMustacheJson(str, requestMap);
        } catch (Throwable e) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_ARRAY_FORMAT");
        }

        return ChangeSetRows.fromJsonNode(jsonNode);
    }

    /**
     * 从命令详细信息中解析出批量记录。
     *
     * @param commandDetail 包含命令详细信息的Map
     * @return 解析出的批量记录
     */
    public static String parseBulkRecords(Map<String, Object> commandDetail) {
        Object o = commandDetail.get(RECORD_FORM_KEY);
        if (!(o instanceof String str)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_CHANGE_SET_EMPTY");
        }
        return str;
    }

    /**
     * 从命令详细信息中解析出主键。
     *
     * @param commandDetail 包含命令详细信息的Map
     * @return 解析出的主键
     */
    public static String parsePrimaryKey(Map<String, Object> commandDetail) {
        Object o = commandDetail.get(PRIMARY_KEY_FORM_KEY);
        if (!(o instanceof String str)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_PRIMARY_KEY_EMPTY");
        }
        return str;
    }

    /**
     * 从原始字符串中提取Mustache键。
     *
     * @return 包含Mustache键的集合
     */
    public Set<String> extractMustacheKeys() {
        return MustacheHelper.extractMustacheKeysWithCurlyBraces(str);
    }
}

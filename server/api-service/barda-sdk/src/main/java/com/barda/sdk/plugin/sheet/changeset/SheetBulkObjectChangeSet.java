package com.barda.sdk.plugin.sheet.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.plugin.common.constant.Constants.RECORD_FORM_KEY;

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.MustacheHelper;

/**
 * 用于处理SheetBulkObjectChangeSet的类。
 * 该类包含了将JSON字符串转换为SheetChangeSetRows的功能。
 */
public class SheetBulkObjectChangeSet {

    /**
     * 要转换的JSON字符串。
     */
    private final String str;

    /**
     * 私有构造函数，用于创建SheetBulkObjectChangeSet实例。
     *
     * @param str 要转换的JSON字符串
     */
    public SheetBulkObjectChangeSet(String str) {
        this.str = str;
    }

    /**
     * 将JSON字符串转换为SheetChangeSetRows。
     *
     * @param requestMap 包含Mustache模板中使用的变量的Map
     * @return 转换后的SheetChangeSetRows
     * @throws PluginException 如果JSON字符串格式不正确
     */
    public SheetChangeSetRows render(Map<String, Object> requestMap) {
        JsonNode jsonNode;
        try {
            jsonNode = MustacheHelper.renderMustacheJson(str, requestMap);
        } catch (Throwable e) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_JSON_ARRAY_FORMAT");
        }

        return SheetChangeSetRows.fromJsonNode(jsonNode);
    }

    /**
     * 从Map中解析SheetBulkObjectChangeSet。
     *
     * @param commandDetail 包含SheetBulkObjectChangeSet的原始Map
     * @return 解析后的SheetBulkObjectChangeSet
     * @throws PluginException 如果原始Map中不包含有效的SheetBulkObjectChangeSet
     */
    public static SheetBulkObjectChangeSet parseBulkRecords(Map<String, Object> commandDetail) {
        Object o = commandDetail.get(RECORD_FORM_KEY);
        if (!(o instanceof String str)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_CHANGE_SET_EMPTY");
        }
        return new SheetBulkObjectChangeSet(str);
    }
}

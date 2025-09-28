package com.barda.sdk.plugin.sheet.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.plugin.common.constant.Constants.CHANGE_SET_FORM_KEY;
import static com.barda.sdk.plugin.common.constant.Constants.CHANGE_SET_TYPE_KEY_VALUE_PAIRS;
import static com.barda.sdk.plugin.common.constant.Constants.CHANGE_SET_TYPE_OBJECT;
import static com.barda.sdk.plugin.common.constant.Constants.COMP_KEY;
import static com.barda.sdk.plugin.common.constant.Constants.COMP_TYPE_KEY;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.barda.sdk.exception.PluginException;

/**
 * 用于表示SheetChangeSet的抽象类。
 * 该类定义了将Map转换为SheetChangeSetRow的抽象方法。
 */
public abstract class SheetChangeSet {

    /**
     * 将Map转换为SheetChangeSetRow。
     *
     * @param requestMap 包含Mustache模板中使用的变量的Map
     * @return 转换后的SheetChangeSetRow
     */
    public abstract SheetChangeSetRow render(Map<String, Object> requestMap);

    /**
     * 从Map中解析SheetChangeSet。
     *
     * @param commandDetail 包含SheetChangeSet的原始Map
     * @return 解析后的SheetChangeSet
     * @throws PluginException 如果原始Map中不包含有效的SheetChangeSet
     */
    public static SheetChangeSet parseChangeSet(Map<String, Object> commandDetail) {

        Object c = commandDetail.get(CHANGE_SET_FORM_KEY);
        if (c instanceof Map<?, ?>) {
            return getFromChangeSet((Map<String, Object>) c);
        }

        throw new PluginException(INVALID_GUI_SETTINGS, "GUI_OPERATION_DATA_EMPTY");
    }

    /**
     * 从Map中获取SheetChangeSet。
     *
     * @param changeSet 包含SheetChangeSet的原始Map
     * @return 获取的SheetChangeSet
     * @throws PluginException 如果原始Map中不包含有效的SheetChangeSet
     */
    private static SheetChangeSet getFromChangeSet(Map<String, Object> changeSet) {
        if (MapUtils.isEmpty(changeSet)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_OPERATION_DATA_EMPTY");
        }

        String changeSetType = MapUtils.getString(changeSet, COMP_TYPE_KEY, "").toUpperCase();
        if (StringUtils.isBlank(changeSetType)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_OPERATION_DATA_TYPE_ERROR");
        }

        Object data = MapUtils.getObject(changeSet, COMP_KEY);

        if (changeSetType.equals(CHANGE_SET_TYPE_KEY_VALUE_PAIRS)) {
            return new SheetKeyValuePairChangeSet(data);
        }

        if (changeSetType.equals(CHANGE_SET_TYPE_OBJECT)) {
            if (!(data instanceof String)) {
                throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_PARAM", data.getClass().getSimpleName());
            }
            return new SheetObjectChangeSet((String) data);
        }

        throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_DATA_TYPE", changeSetType);
    }
}

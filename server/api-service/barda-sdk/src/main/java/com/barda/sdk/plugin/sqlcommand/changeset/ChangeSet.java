package com.barda.sdk.plugin.sqlcommand.changeset;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.plugin.common.constant.Constants.CHANGE_SET_FORM_KEY;
import static com.barda.sdk.plugin.common.constant.Constants.CHANGE_SET_TYPE_KEY_VALUE_PAIRS;
import static com.barda.sdk.plugin.common.constant.Constants.CHANGE_SET_TYPE_OBJECT;
import static com.barda.sdk.plugin.common.constant.Constants.COMP_KEY;
import static com.barda.sdk.plugin.common.constant.Constants.COMP_TYPE_KEY;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.barda.sdk.exception.PluginException;

/**
 * 用于处理更改集的抽象类。
 * 该类包含了一些公共方法，用于解析、渲染和提取Mustache键。
 */
public abstract class ChangeSet {

    /**
     * 解析命令详细信息中的更改集。
     *
     * @param commandDetail 包含命令详细信息的Map
     * @return 解析出的更改集
     */
    public static ChangeSet parseChangeSet(Map<String, Object> commandDetail) {
        return parseChangeSet(commandDetail, CHANGE_SET_FORM_KEY);
    }

    /**
     * 解析命令详细信息中的更改集。
     *
     * @param commandDetail 包含命令详细信息的Map
     * @param keyName        用于在命令详细信息中查找更改集的键名
     * @return 解析出的更改集
     */
    @SuppressWarnings("unchecked")
    public static ChangeSet parseChangeSet(Map<String, Object> commandDetail, String keyName) {
        Object o = commandDetail.get(keyName);
        if (!(o instanceof Map<?, ?>)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_OPERATION_DATA_EMPTY");
        }
        Map<String, Object> changeSet = (Map<String, Object>) o;
        if (MapUtils.isEmpty(changeSet)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_OPERATION_DATA_EMPTY");
        }

        String changeSetType = MapUtils.getString(changeSet, COMP_TYPE_KEY, "").toUpperCase();
        if (StringUtils.isBlank(changeSetType)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_OPERATION_DATA_TYPE_ERROR");
        }

        Object data = MapUtils.getObject(changeSet, COMP_KEY);

        if (changeSetType.equals(CHANGE_SET_TYPE_KEY_VALUE_PAIRS)) {
            return new KeyValuePairChangeSet(data);
        }

        if (changeSetType.equals(CHANGE_SET_TYPE_OBJECT)) {
            if (!(data instanceof String)) {
                throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_PARAM", data.getClass().getSimpleName());
            }
            return new ObjectChangeSet((String) data);
        }

        throw new PluginException(INVALID_GUI_SETTINGS, "GUI_INVALID_DATA_TYPE", changeSetType);
    }

    /**
     * 将原始数据渲染为ChangeSetRow。
     *
     * @param requestMap 包含渲染所需的数据的Map
     * @return 渲染后的ChangeSetRow
     */
    public abstract ChangeSetRow render(Map<String, Object> requestMap);

    /**
     * 从原始数据中提取Mustache键。
     *
     * @return 包含Mustache键的集合
     */
    public abstract Set<String> extractMustacheKeys();
}

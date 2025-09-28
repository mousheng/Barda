package com.barda.domain.group.util;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;
import com.barda.sdk.util.LocaleUtils;

/**
 * 系统群组的常量和工具类。
 */
public class SystemGroups {

    /**
     * 所有用户群组的类型。
     */
    public static String ALL_USER = "all";

    /**
     * 开发者群组的类型。
     */
    public static String DEV = "dev";

    /**
     * 系统群组名称的映射。
     */
    private static final Map<String, String> SYSTEM_GROUP_NAME_MAP = ImmutableMap.of(
            ALL_USER, "SYSTEM_GROUP_ALL_USER",
            DEV, "SYSTEM_GROUP_DEV"
    );

    /**
     * 获取指定类型和语言环境的系统群组名称。
     *
     * @param type 群组类型
     * @param locale 语言环境
     * @return 系统群组名称，如果类型不在映射中，返回 null
     */
    public static String getName(String type, Locale locale) {
        String key = SYSTEM_GROUP_NAME_MAP.get(type);
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return LocaleUtils.getMessage(locale, key);
    }
}

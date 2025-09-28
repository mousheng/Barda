package com.barda.domain.permission.model;

import static com.barda.domain.permission.config.PermissionConst.ID_SPLITTER;
import static com.barda.sdk.util.StreamUtils.collectMap;

import java.util.Arrays;
import java.util.Map;

/**
 * 资源类型枚举类。
 * 该类表示可以拥有特定资源的不同类型。
 */
public enum ResourceType {

    /**
     * 应用程序。
     */
    APPLICATION("app"),

    /**
     * 数据源。
     */
    DATASOURCE("data"),

    /**
     * 文件夹。
     */
    FOLDER("folder");

    private final String abbr;

    private static final Map<String, ResourceType> MAP;

    static {
        MAP = collectMap(Arrays.stream(values()), it -> it.abbr);
    }

    ResourceType(String abbr) {
        this.abbr = abbr;
    }

    /**
     * 将资源 ID 与缩写连接在一起。
     *
     * @param resourceId 资源 ID
     * @return 连接后的字符串
     */
    public String join(String resourceId) {
        return abbr + ID_SPLITTER + resourceId;
    }

    /**
     * 从缩写中获取 ResourceType 实例。
     *
     * @param abbr 缩写
     * @return ResourceType 实例，如果找不到匹配的缩写，则返回 null
     */
    public static ResourceType from(String abbr) {
        return MAP.get(abbr);
    }
}

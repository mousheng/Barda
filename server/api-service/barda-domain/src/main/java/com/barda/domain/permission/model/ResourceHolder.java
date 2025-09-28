package com.barda.domain.permission.model;

import static com.barda.domain.permission.config.PermissionConst.ID_SPLITTER;

import java.util.Arrays;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

/**
 * 资源持有者枚举类。
 * 该枚举类定义了可以拥有资源的实体类型，并为每种类型提供了一个缩写。
 */
public enum ResourceHolder {

    /**
     * 用户。
     * 缩写为 "u"。
     */
    USER("u"),

    /**
     * 群组。
     * 缩写为 "g"。
     */
    GROUP("g"),
    ;

    private final String abbr;

    /**
     * 构造函数。
     *
     * @param abbr 缩写
     */
    ResourceHolder(String abbr) {
        this.abbr = abbr;
    }

    /**
     * 将 ID 与缩写连接在一起。
     *
     * @param id ID
     * @return 连接后的字符串
     */
    public String join(String id) {
        return abbr + ID_SPLITTER + id;
    }

    /**
     * 从缩写中获取 ResourceHolder 实例。
     *
     * @param abbr 缩写
     * @return ResourceHolder 实例，如果找不到匹配的缩写，则返回 null
     */
    @Nullable
    public static ResourceHolder from(String abbr) {
        return Arrays.stream(values())
                .filter(it -> StringUtils.equalsIgnoreCase(it.abbr, abbr))
                .findFirst()
                .orElse(null);
    }
}

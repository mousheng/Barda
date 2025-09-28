package com.barda.domain.organization.model;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织成员角色枚举。
 * 该枚举定义了组织中成员可以拥有的不同角色。
 */
public enum MemberRole {

    /**
     * 普通成员。
     */
    MEMBER("member"),

    /**
     * 管理员。
     */
    ADMIN("admin");

    /**
     * 用于快速查找枚举值的映射。
     */
    private static final Map<String, MemberRole> VALUE_MAP;

    static {
        VALUE_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(MemberRole::getValue, it -> it));
    }

    /**
     * 枚举值的字符串表示。
     */
    private final String value;

    MemberRole(String value) {
        this.value = value;
    }

    /**
     * 获取枚举值的字符串表示。
     *
     * @return 枚举值的字符串表示
     */
    public String getValue() {
        return value;
    }

    /**
     * 根据字符串值获取枚举值。
     * 如果找不到匹配的枚举值，则返回默认值 MEMBER。
     *
     * @param str 字符串值
     * @return 枚举值
     */
    public static MemberRole fromValue(String str) {
        return VALUE_MAP.getOrDefault(str, MEMBER);
    }

    /**
     * 判断是否为管理员。
     *
     * @param str 字符串值
     * @return true - 是管理员，false - 不是管理员
     */
    public static boolean isAdmin(String str) {
        return ADMIN == fromValue(str);
    }
}

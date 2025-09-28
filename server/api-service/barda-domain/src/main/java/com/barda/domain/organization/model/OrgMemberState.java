package com.barda.domain.organization.model;

/**
 * 组织成员状态枚举。
 * 该枚举定义了组织成员可以拥有的不同状态。
 */
public enum OrgMemberState {

    /**
     * 邀请中状态。
     * 指示该成员处于邀请中的状态，尚未加入组织。
     */
    INVITING("inviting"),

    /**
     * 正常状态。
     * 指示该成员处于正常状态，已加入组织。
     */
    NORMAL("normal"),

    /**
     * 当前状态。
     * 指示该成员为当前组织的成员。
     */
    CURRENT("current"),

    ;

    /**
     * 枚举值的字符串表示。
     */
    private final String value;

    /**
     * 构造函数。
     *
     * @param value 枚举值的字符串表示
     */
    OrgMemberState(String value) {
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
}

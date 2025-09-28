package com.barda.domain.user.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 该枚举类型定义了用户状态的不同类型。
 * 它提供了一个静态方法来根据输入值返回对应的枚举值。
 */
public enum UserStatusType {

    HAS_SHOW_NEW_USER_GUIDANCE("newUserGuidance"), // 已展示新手指南
    NON_DEV_POP_UP_FOR_OLD_USERS("olderUserNonDevPopup"), // 非开发者向旧用户弹出的窗口
    ;

    private static final Map<String, UserStatusType> VALUE_MAP;

    static {
        VALUE_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(UserStatusType::getValue, it -> it));
    }

    private final String value;

    UserStatusType(String value) {
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
     * 根据输入值返回对应的枚举值。
     *
     * @param input 输入值
     * @return 对应的枚举值，如果找不到匹配的枚举值，返回 null
     */
    public static UserStatusType fromValue(String input) {
        return VALUE_MAP.get(input);
    }
}

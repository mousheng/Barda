package com.barda.domain.user.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;

/**
 * 用户详细信息类，使用Builder模式创建实例。
 */
@Builder
@Getter
public class UserDetail {

    /**
     * 匿名用户的默认实例
     */
    public static final UserDetail ANONYMOUS_CURRENT_USER = UserDetail.builder()
            .id("")
            .name("ANONYMOUS")
            .avatarUrl("")
            .email("")
            .ip("")
            .groups(Collections.emptyList())
            .extra(Collections.emptyMap())
            .build();

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户IP
     */
    private String ip;

    /**
     * 用户所属的组列表
     */
    private List<Map<String, String>> groups;

    /**
     * 额外的用户信息
     */
    private Map<String, Object> extra;
}

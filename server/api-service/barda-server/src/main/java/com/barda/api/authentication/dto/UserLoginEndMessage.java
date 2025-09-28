package com.barda.api.authentication.dto;

import com.barda.domain.user.model.User;

import lombok.Builder;
import lombok.Getter;

/**
 * 用户登录结束消息类。
 * 该类使用 Lombok 的 {@link Getter} 和 {@link Builder} 注解来生成 getter 方法和构建器。
 */
@Getter
@Builder
public class UserLoginEndMessage {

    /**
     * 登录的用户。
     */
    private User user;

    /**
     * 是否为新用户注册。
     */
    private boolean signUp;

    /**
     * 新用户注册来源。
     */
    private String signUpSource;

    /**
     * 请求的域名。
     */
    private String requestDomain;

    /**
     * 第三方登录机构的 ID。
     */
    private String thirdPartyLoginOrgId;
}

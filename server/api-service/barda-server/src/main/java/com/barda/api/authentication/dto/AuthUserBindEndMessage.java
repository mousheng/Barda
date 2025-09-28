package com.barda.api.authentication.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 身份验证用户绑定结束消息类。
 * 该类使用 Lombok 的 {@link Getter} 和 {@link Builder} 注解来生成 getter 方法和构建器。
 */
@Getter
@Builder
public class AuthUserBindEndMessage {

    /**
     * 绑定操作是否成功。
     */
    private boolean success;

    /**
     * 被绑定的域名。
     */
    private String bindDomain;
}

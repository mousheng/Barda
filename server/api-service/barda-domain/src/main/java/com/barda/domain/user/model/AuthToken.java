package com.barda.domain.user.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 授权令牌类，用于表示授权令牌的相关信息。
 * 该类使用了 Lombok 库来自动生成 getter、setter、构造函数和构建器。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken implements Serializable {

    /**
     * 访问令牌。
     */
    private String accessToken;

    /**
     * 访问令牌的过期时间（单位：秒）。
     */
    private int expireIn;

    /**
     * 刷新令牌。
     */
    private String refreshToken;

    /**
     * 刷新令牌的过期时间（单位：秒）。
     */
    private int refreshTokenExpireIn;

    /**
     * OpenID。
     */
    private String openId;

    /**
     * 授权码。
     */
    private String code;
}

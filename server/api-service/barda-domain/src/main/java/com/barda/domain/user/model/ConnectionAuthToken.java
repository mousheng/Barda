package com.barda.domain.user.model;

import lombok.Builder;
import lombok.Data;

/**
 * 数据对象，表示连接的认证令牌。
 *
 * @see AuthToken 业务对象
 */
@Data
@Builder
public class ConnectionAuthToken {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 访问令牌过期时间
     */
    private Long expireAt;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 刷新令牌过期时间
     */
    private Long refreshTokenExpireAt;

    /**
     * 废弃的来源（已弃用）
     */
    @Deprecated
    private String source;

    /**
     * 判断访问令牌是否已过期
     * @return boolean
     */
    public boolean isAccessTokenExpired() {
        return expireAt == null || expireAt < System.currentTimeMillis() / 1000;
    }

    /**
     * 判断刷新令牌是否已过期
     * @return boolean
     */
    public boolean isRefreshTokenExpired() {
        return refreshTokenExpireAt == null || refreshTokenExpireAt < System.currentTimeMillis() / 1000;
    }

    /**
     * 从 AuthToken 创建 ConnectionAuthToken
     * @param token AuthToken
     * @return ConnectionAuthToken
     */
    public static ConnectionAuthToken of(AuthToken token) {
        return ConnectionAuthToken.builder()
                .accessToken(token.getAccessToken())
                .expireAt(System.currentTimeMillis() / 1000 + token.getExpireIn() - 60)
                .refreshToken(token.getRefreshToken())
                .refreshTokenExpireAt(System.currentTimeMillis() / 1000 + token.getRefreshTokenExpireIn() - 60)
                .source(null)
                .build();
    }
}
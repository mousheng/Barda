package com.barda.domain.user.model;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.barda.domain.authentication.context.AuthRequestContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 授权用户类，用于表示授权用户的相关信息。
 * 该类使用了 Lombok 库来自动生成 getter、setter、构造函数和构建器。
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    /**
     * 用户的唯一标识符。
     */
    private String uid;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 用户头像的 URL。
     */
    private String avatar;

    /**
     * 原始的用户信息。
     */
    private Map<String, Object> rawUserInfo;

    /**
     * 额外的用户信息。
     */
    private Map<String, Object> extra;

    /**
     * 组织 ID。
     */
    private String orgId;

    /**
     * 授权请求的上下文。
     */
    private AuthRequestContext authContext;

    /**
     * 授权令牌。
     * 在用户通过 OAuth 2.0 进行身份验证时，我们存储令牌信息，以便在将来的数据源或查询中使用。
     */
    @Nullable
    private AuthToken authToken;

    /**
     * 获取授权来源。
     *
     * @return 授权来源
     */
    public String getSource() {
        return getAuthContext().getAuthConfig().getSource();
    }

    /**
     * 将授权用户转换为授权连接。
     *
     * @return 授权连接
     */
    public Connection toAuthConnection() {
        return Connection.builder()
                .authId(getAuthContext().getAuthConfig().getId())
                .source(getSource())
                .name(getUsername())
                .rawId(getUid())
                .avatar(getAvatar())
                .orgIds(StringUtils.isBlank(getOrgId()) ? Set.of() : Set.of(getOrgId()))
                .authConnectionAuthToken(Optional.ofNullable(authToken).map(ConnectionAuthToken::of).orElse(null))
                .rawUserInfo(getRawUserInfo())
                .build();
    }
}

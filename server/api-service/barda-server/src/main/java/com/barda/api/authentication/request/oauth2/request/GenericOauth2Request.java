package com.barda.api.authentication.request.oauth2.request;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.BodyInserters;

import com.barda.api.authentication.request.AuthException;
import com.barda.api.authentication.request.oauth2.OAuth2RequestContext;
import com.barda.api.authentication.request.oauth2.Oauth2DefaultSource;
import com.barda.domain.user.model.AuthToken;
import com.barda.domain.user.model.AuthUser;
import com.barda.sdk.auth.GenericOauth2AuthConfig;
import com.barda.sdk.util.JsonUtils;
import com.barda.sdk.webclient.WebClientBuildHelper;

import reactor.core.publisher.Mono;

/**
 * 通用 OAuth2/OIDC 登录请求处理器。
 * <p>
 * 支持任意符合标准 OAuth2/OIDC 协议的身份提供商，所有端点 URL 和用户字段映射均可动态配置。
 */
public class GenericOauth2Request extends AbstractOauth2Request<GenericOauth2AuthConfig> {

    public GenericOauth2Request(GenericOauth2AuthConfig config) {
        super(config, null);
    }

    @Override
    protected Mono<AuthToken> getAuthToken(OAuth2RequestContext context) {
        return WebClientBuildHelper.builder()
                .systemProxy()
                .build()
                .post()
                .uri(config.getTokenEndpoint())
                .header(HttpHeaders.ACCEPT, "application/json")
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("code", context.getCode())
                        .with("client_id", config.getClientId())
                        .with("client_secret", config.getClientSecret())
                        .with("redirect_uri", context.getRedirectUrl()))
                .exchangeToMono(response -> response.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                }))
                .flatMap(map -> {
                    // 兼容 camelCase 和 snake_case 响应格式
                    String accessToken = getStringFromMap(map, "access_token", "accessToken");
                    if (StringUtils.isBlank(accessToken)) {
                        return Mono.error(new AuthException("Generic OAuth2 token response missing access_token: "
                                + JsonUtils.toJson(map)));
                    }
                    String refreshToken = getStringFromMap(map, "refresh_token", "refreshToken");
                    int expiresIn = getIntFromMap(map, "expires_in", "expireIn");
                    String scope = getStringFromMap(map, "scope");
                    AuthToken authToken = AuthToken.builder()
                            .accessToken(accessToken)
                            .expireIn(expiresIn)
                            .refreshToken(refreshToken)
                            .openId(getStringFromMap(map, "id_token"))
                            .build();
                    return Mono.just(authToken);
                })
                .onErrorMap(e -> e instanceof AuthException ? e
                        : new AuthException("Generic OAuth2 token exchange failed: " + e.getMessage()));
    }

    @Override
    protected Mono<AuthUser> getAuthUser(AuthToken authToken) {
        return WebClientBuildHelper.builder()
                .systemProxy()
                .build()
                .get()
                .uri(config.getUserInfoEndpoint())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken.getAccessToken())
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new AuthException(
                                        "Generic OAuth2 user info error: HTTP " + response.statusCode().value()
                                                + ", body=" + body)));
                    }
                    return response.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
                })
                .flatMap(map -> {
                    Map<String, String> mappings = config.getSourceMappings();
                    String uid = extractMappedField(map, mappings, "uid",
                            "sub", "openId", "id", "user_id");
                    if (StringUtils.isBlank(uid)) {
                        return Mono.error(new AuthException(
                                "Generic OAuth2 user info: unable to determine user id from response"));
                    }
                    String username = extractMappedField(map, mappings, "username",
                            "name", "preferred_username", "login", "nick");
                    if (StringUtils.isBlank(username)) {
                        username = uid;
                    }
                    String email = extractMappedField(map, mappings, "email");
                    String avatar = extractMappedField(map, mappings, "avatar",
                            "picture", "avatar_url", "avatarUrl");
                    authToken.setOpenId(uid);
                    AuthUser authUser = AuthUser.builder()
                            .uid(uid)
                            .username(username)
                            .avatar(avatar)
                            .rawUserInfo(map)
                            .build();
                    if (StringUtils.isNotBlank(email)) {
                        authUser.setExtra(Map.of("email", email));
                    }
                    return Mono.just(authUser);
                });
    }

    /**
     * 根据 sourceMappings 从响应中提取字段值。
     * <p>
     * 优先使用 mappings 中配置的 key，然后依次尝试 fallbackKeys。
     */
    private String extractMappedField(Map<String, Object> map, Map<String, String> mappings,
            String mappingKey, String... fallbackKeys) {
        if (mappings != null && mappings.containsKey(mappingKey)) {
            String mappedKey = mappings.get(mappingKey);
            if (StringUtils.isNotBlank(mappedKey)) {
                String value = MapUtils.getString(map, mappedKey);
                if (StringUtils.isNotBlank(value)) {
                    return value;
                }
            }
        }
        for (String fallback : fallbackKeys) {
            String value = MapUtils.getString(map, fallback);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 从 map 中获取字符串值，优先尝试 snake_case key，然后尝试 camelCase key。
     */
    private String getStringFromMap(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            String value = MapUtils.getString(map, key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 从 map 中获取整数值，优先尝试 snake_case key，然后尝试 camelCase key。
     */
    private int getIntFromMap(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Integer value = MapUtils.getInteger(map, key);
            if (value != null) {
                return value;
            }
        }
        return 0;
    }
}

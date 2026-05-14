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
import com.barda.sdk.auth.Oauth2SimpleAuthConfig;
import com.barda.sdk.util.JsonUtils;
import com.barda.sdk.webclient.WebClientBuildHelper;

import reactor.core.publisher.Mono;

/**
 * 飞书 OAuth2 登录请求。
 */
public class FeishuRequest extends AbstractOauth2Request<Oauth2SimpleAuthConfig> {

    public FeishuRequest(Oauth2SimpleAuthConfig config) {
        super(config, Oauth2DefaultSource.FEISHU);
    }

    @Override
    protected Mono<AuthToken> getAuthToken(OAuth2RequestContext context) {
        // 飞书 OAuth2 API 需要将 app_id 和 app_secret 作为 JSON body 参数发送
        Map<String, Object> body = Map.of(
                "grant_type", "authorization_code",
                "code", context.getCode(),
                "app_id", config.getClientId(),
                "app_secret", config.getClientSecret());

        return WebClientBuildHelper.builder()
                .systemProxy()
                .build()
                .post()
                .uri(source.accessToken())
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(BodyInserters.fromValue(body))
                .exchangeToMono(response -> response.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                }))
                .flatMap(map -> {
                    Integer code = MapUtils.getInteger(map, "code");
                    Map<String, Object> data = map.containsKey("data") && map.get("data") instanceof Map
                            ? (Map<String, Object>) map.get("data") : map;
                    if (code != null && code != 0) {
                        String msg = MapUtils.getString(map, "msg", JsonUtils.toJson(map));
                        return Mono.error(new AuthException("Feishu API error: code=" + code + ", msg=" + msg));
                    }
                    String accessToken = MapUtils.getString(data, "access_token");
                    if (StringUtils.isBlank(accessToken)) {
                        return Mono.error(new AuthException("empty access_token"));
                    }
                    AuthToken authToken = AuthToken.builder()
                            .accessToken(accessToken)
                            .expireIn(MapUtils.getIntValue(data, "expires_in"))
                            .refreshToken(MapUtils.getString(data, "refresh_token"))
                            .refreshTokenExpireIn(MapUtils.getIntValue(data, "refresh_expires_in"))
                            .openId(MapUtils.getString(data, "open_id"))
                            .build();
                    return Mono.just(authToken);
                });
    }

    @Override
    protected Mono<AuthUser> getAuthUser(AuthToken authToken) {
        return WebClientBuildHelper.builder()
                .systemProxy()
                .build()
                .get()
                .uri(source.userInfo())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken.getAccessToken())
                .exchangeToMono(response -> response.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                }))
                .flatMap(map -> {
                    Integer code = MapUtils.getInteger(map, "code");
                    Map<String, Object> data = map.containsKey("data") && map.get("data") instanceof Map
                            ? (Map<String, Object>) map.get("data") : map;
                    if (code != null && code != 0) {
                        String msg = MapUtils.getString(map, "msg", JsonUtils.toJson(map));
                        return Mono.error(new AuthException("Feishu API error: code=" + code + ", msg=" + msg));
                    }
                    String uid = MapUtils.getString(data, "open_id");
                    if (StringUtils.isBlank(uid)) {
                        uid = MapUtils.getString(data, "user_id");
                    }
                    if (StringUtils.isBlank(uid)) {
                        return Mono.error(new AuthException("empty user id"));
                    }
                    String username = MapUtils.getString(data, "name");
                    if (StringUtils.isBlank(username)) {
                        username = MapUtils.getString(data, "en_name");
                    }
                    if (StringUtils.isBlank(username)) {
                        username = uid; // 使用 open_id 作为用户名
                    }
                    AuthUser authUser = AuthUser.builder()
                            .uid(uid)
                            .username(username)
                            .avatar(MapUtils.getString(data, "avatar_url"))
                            .rawUserInfo(data)
                            .build();
                    return Mono.just(authUser);
                });
    }
}



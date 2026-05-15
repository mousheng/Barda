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
 * 钉钉 OAuth2 登录请求。
 */
public class DingTalkRequest extends AbstractOauth2Request<Oauth2SimpleAuthConfig> {

    public DingTalkRequest(Oauth2SimpleAuthConfig config) {
        super(config, Oauth2DefaultSource.DINGTALK);
    }

    @Override
    protected Mono<AuthToken> getAuthToken(OAuth2RequestContext context) {
        Map<String, Object> body = Map.of(
                "grantType", "authorization_code",
                "code", context.getCode(),
                "clientId", config.getClientId(),
                "clientSecret", config.getClientSecret()
        );

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
                    String accessToken = MapUtils.getString(map, "accessToken");
                    if (StringUtils.isBlank(accessToken)) {
                        String code = MapUtils.getString(map, "code");
                        String msg = MapUtils.getString(map, "message", JsonUtils.toJson(map));
                        return Mono.error(new AuthException("DingTalk token error: code=" + code + ", msg=" + msg));
                    }
                    AuthToken authToken = AuthToken.builder()
                            .accessToken(accessToken)
                            .expireIn(MapUtils.getIntValue(map, "expireIn"))
                            .refreshToken(MapUtils.getString(map, "refreshToken"))
                            .refreshTokenExpireIn(MapUtils.getIntValue(map, "refreshTokenExpireIn"))
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
                .header("x-acs-dingtalk-access-token", authToken.getAccessToken())
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new AuthException(
                                        "DingTalk user info error: HTTP " + response.statusCode().value() + ", body=" + body)));
                    }
                    return response.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
                })
                .flatMap(map -> {
                    String uid = MapUtils.getString(map, "openId");
                    if (StringUtils.isBlank(uid)) {
                        uid = MapUtils.getString(map, "unionId");
                    }
                    if (StringUtils.isBlank(uid)) {
                        return Mono.error(new AuthException("DingTalk user info: empty openId and unionId"));
                    }
                    String name = MapUtils.getString(map, "nick");
                    if (StringUtils.isBlank(name)) {
                        name = uid;
                    }
                    authToken.setOpenId(uid);
                    AuthUser authUser = AuthUser.builder()
                            .uid(uid)
                            .username(name)
                            .avatar(MapUtils.getString(map, "avatarUrl"))
                            .rawUserInfo(map)
                            .build();
                    return Mono.just(authUser);
                });
    }
}

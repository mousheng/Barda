package com.barda.api.authentication.request.oauth2;

import static com.barda.sdk.auth.constants.AuthTypeConstants.GITHUB;
import static com.barda.sdk.auth.constants.AuthTypeConstants.GOOGLE;
import static com.barda.sdk.auth.constants.AuthTypeConstants.FEISHU;
import static com.barda.sdk.auth.constants.AuthTypeConstants.DINGTALK;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.barda.api.authentication.request.AuthRequest;
import com.barda.api.authentication.request.AuthRequestFactory;
import com.barda.api.authentication.request.oauth2.request.AbstractOauth2Request;
import com.barda.api.authentication.request.oauth2.request.GithubRequest;
import com.barda.api.authentication.request.oauth2.request.GoogleRequest;
import com.barda.api.authentication.request.oauth2.request.FeishuRequest;
import com.barda.api.authentication.request.oauth2.request.DingTalkRequest;
import com.barda.sdk.auth.Oauth2SimpleAuthConfig;

import reactor.core.publisher.Mono;

@Component
public class Oauth2AuthRequestFactory implements AuthRequestFactory<OAuth2RequestContext> {

    @Override
    public Mono<AuthRequest> build(OAuth2RequestContext context) {
        return Mono.fromSupplier(() -> buildRequest(context));
    }

    private AbstractOauth2Request<? extends Oauth2SimpleAuthConfig> buildRequest(OAuth2RequestContext context) {
        return switch (context.getAuthConfig().getAuthType()) {
            case GITHUB -> new GithubRequest((Oauth2SimpleAuthConfig) context.getAuthConfig());
            case GOOGLE -> new GoogleRequest((Oauth2SimpleAuthConfig) context.getAuthConfig());
            case FEISHU -> new FeishuRequest((Oauth2SimpleAuthConfig) context.getAuthConfig());
            case DINGTALK -> new DingTalkRequest((Oauth2SimpleAuthConfig) context.getAuthConfig());
            default -> throw new UnsupportedOperationException(context.getAuthConfig().getAuthType());
        };
    }

    @Override
    public Set<String> supportedAuthTypes() {
        return Set.of(
                GITHUB,
                GOOGLE,
                FEISHU,
                DINGTALK);
    }
}

package com.barda.sdk.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.barda.sdk.auth.constants.Oauth2Constants;
import com.barda.sdk.config.SerializeConfig.JsonViews;

import lombok.Getter;

/**
 * 通用 OAuth2/OIDC 身份验证配置。
 * <p>
 * 支持任意符合 OAuth2/OIDC 标准的身份提供商，端点 URL 和字段映射均可动态配置。
 */
@Getter
public class GenericOauth2AuthConfig extends Oauth2SimpleAuthConfig {

    private String issuerUri;
    private String authorizationEndpoint;
    private String tokenEndpoint;
    private String userInfoEndpoint;
    private String jwksUri;
    private String scope;

    private String sourceDescription;
    private String sourceIcon;

    private Map<String, String> sourceMappings;

    private Boolean userCanSelectAccounts;

    @JsonCreator
    public GenericOauth2AuthConfig(
            @Nullable String id,
            Boolean enable,
            Boolean enableRegister,
            String source,
            String sourceName,
            String clientId,
            String clientSecret,
            String authType,
            String issuerUri,
            String authorizationEndpoint,
            String tokenEndpoint,
            String userInfoEndpoint,
            @Nullable String jwksUri,
            String scope,
            @Nullable String sourceDescription,
            @Nullable String sourceIcon,
            @Nullable Map<String, String> sourceMappings,
            @JsonProperty(required = false) @Nullable Boolean userCanSelectAccounts) {
        super(id, enable, enableRegister, source, sourceName, clientId, clientSecret, authType);
        this.issuerUri = issuerUri;
        this.authorizationEndpoint = authorizationEndpoint;
        this.tokenEndpoint = tokenEndpoint;
        this.userInfoEndpoint = userInfoEndpoint;
        this.jwksUri = jwksUri;
        this.scope = scope;
        this.sourceDescription = sourceDescription;
        this.sourceIcon = sourceIcon;
        this.sourceMappings = sourceMappings != null ? sourceMappings : new HashMap<>();
        this.userCanSelectAccounts = BooleanUtils.isTrue(userCanSelectAccounts);
    }

    @Override
    @JsonView(JsonViews.Public.class)
    public String getAuthorizeUrl() {
        String url = authorizationEndpoint
                + "?response_type=code"
                + "&client_id=" + Oauth2Constants.CLIENT_ID_PLACEHOLDER
                + "&redirect_uri=" + Oauth2Constants.REDIRECT_URL_PLACEHOLDER
                + "&scope=" + (StringUtils.isNotBlank(scope) ? scope : "openid")
                + "&state=" + Oauth2Constants.STATE_PLACEHOLDER;
        if (BooleanUtils.isTrue(userCanSelectAccounts)) {
            url += "&prompt=login";
        }
        return url;
    }

    @Override
    public void doEncrypt(Function<String, String> encryptFunc) {
        super.doEncrypt(encryptFunc);
    }

    @Override
    public void doDecrypt(Function<String, String> decryptFunc) {
        super.doDecrypt(decryptFunc);
    }

    @Override
    public void merge(AbstractAuthConfig oldConfig) {
        super.merge(oldConfig);
        if (oldConfig instanceof GenericOauth2AuthConfig oldGenericConfig) {
            if (StringUtils.isBlank(this.issuerUri)) {
                this.issuerUri = oldGenericConfig.getIssuerUri();
            }
            if (StringUtils.isBlank(this.authorizationEndpoint)) {
                this.authorizationEndpoint = oldGenericConfig.getAuthorizationEndpoint();
            }
            if (StringUtils.isBlank(this.tokenEndpoint)) {
                this.tokenEndpoint = oldGenericConfig.getTokenEndpoint();
            }
            if (StringUtils.isBlank(this.userInfoEndpoint)) {
                this.userInfoEndpoint = oldGenericConfig.getUserInfoEndpoint();
            }
            if (StringUtils.isBlank(this.scope)) {
                this.scope = oldGenericConfig.getScope();
            }
            if ((this.sourceMappings == null || this.sourceMappings.isEmpty())
                    && oldGenericConfig.getSourceMappings() != null) {
                this.sourceMappings = oldGenericConfig.getSourceMappings();
            }
        }
    }
}

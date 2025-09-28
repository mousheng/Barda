package com.barda.sdk.auth.constants;

/**
 * 该类包含与 OAuth2 相关的常量。
 */
public class Oauth2Constants {

    /**
     * 客户端 ID 占位符。
     */
    public static final String CLIENT_ID_PLACEHOLDER = "$CLIENT_ID";
    /**
     * 重定向 URL 占位符。
     */
    public static final String REDIRECT_URL_PLACEHOLDER = "$REDIRECT_URL";
    /**
     * 状态占位符。
     */
    public static final String STATE_PLACEHOLDER = "$STATE";

    /**
     * GitHub 授权URL。
     */
    public static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize"
            + "?response_type=code"
            + "&client_id=" + CLIENT_ID_PLACEHOLDER
            + "&redirect_uri=" + REDIRECT_URL_PLACEHOLDER
            + "&state=" + STATE_PLACEHOLDER
            + "&scope=";
    /**
     * Google 授权URL。
     */
    public static final String GOOGLE_AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth"
            + "?response_type=code"
            + "&client_id=" + CLIENT_ID_PLACEHOLDER
            + "&redirect_uri=" + REDIRECT_URL_PLACEHOLDER
            + "&state=" + STATE_PLACEHOLDER
            + "&access_type=offline"
            + "&scope=openid email profile"
            + "&prompt=select_account";
}

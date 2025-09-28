package com.barda.infra.constant;

/**
 * URL 类，提供对 URL 的常量访问。
 *
 * 该类包含了用于构建 URL 的常量字符串，并提供对这些常量的访问。
 *
 */
public final class Url {

    /**
     * 用于构建基础 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api" 前缀。
     */
    public static final String BASE_URL = "/api";

    /**
     * 用于构建版本号的常量。
     *
     * 该常量用于在 URL 前添加 "/v1" 前缀。
     */
    public static final String VERSION = "/v1";

    /**
     * 用于构建组织 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/organizations" 前缀。
     */
    public static final String ORGANIZATION_URL = BASE_URL + VERSION + "/organizations";

    /**
     * 用于构建数据源 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/datasources" 前缀。
     */
    public static final String DATASOURCE_URL = BASE_URL + VERSION + "/datasources";

    /**
     * 用于构建用户 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/users" 前缀。
     */
    public static final String USER_URL = BASE_URL + VERSION + "/users";

    /**
     * 用于构建配置 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/configs" 前缀。
     */
    public static final String CONFIG_URL = BASE_URL + VERSION + "/configs";

    /**
     * 用于构建组 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/groups" 前缀。
     */
    public static final String GROUP_URL = BASE_URL + VERSION + "/groups";

    /**
     * 用于构建资产 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/assets" 前缀。
     */
    public static final String ASSET_URL = BASE_URL + VERSION + "/assets";

    /**
     * 用于构建自定义认证 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/auth" 前缀。
     */
    public static final String CUSTOM_AUTH = BASE_URL + "/auth";

    /**
     * 用于构建邀请 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/invitation" 前缀。
     */
    public static final String INVITATION_URL = BASE_URL + VERSION + "/invitation";

    /**
     * 用于构建应用 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/applications" 前缀。
     */
    public static final String APPLICATION_URL = BASE_URL + VERSION + "/applications";

    /**
     * 用于构建查询 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/query" 前缀。
     */
    public static final String QUERY_URL = BASE_URL + VERSION + "/query";

    /**
     * 用于构建状态 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/v1/state" 前缀。
     */
    public static final String STATE_URL = BASE_URL + VERSION + "/state";
}

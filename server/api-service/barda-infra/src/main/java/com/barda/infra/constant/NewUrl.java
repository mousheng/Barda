package com.barda.infra.constant;

/**
 * 新 URL 类，提供对新 URL 的常量访问。
 *
 * 该类包含了用于构建新 URL 的常量字符串，并提供对这些常量的访问。
 *
 */
public final class NewUrl {

    /**
     * 私有构造函数，防止从外部创建 NewUrl 类的实例。
     *
     */
    private NewUrl() {
    }

    /**
     * 用于构建新 URL 的前缀常量。
     *
     * 该常量用于在 URL 前添加 "/api" 前缀。
     */
    public static final String PREFIX = "/api";

    /**
     * 用于构建组织 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/organizations" 前缀。
     */
    public static final String ORGANIZATION_URL = PREFIX + "/organizations";

    /**
     * 用于构建数据源 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/datasources" 前缀。
     */
    public static final String DATASOURCE_URL = PREFIX + "/datasources";

    /**
     * 用于构建用户 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/users" 前缀。
     */
    public static final String USER_URL = PREFIX + "/users";

    /**
     * 用于构建配置 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/configs" 前缀。
     */
    public static final String CONFIG_URL = PREFIX + "/configs";

    /**
     * 用于构建组 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/groups" 前缀。
     */
    public static final String GROUP_URL = PREFIX + "/groups";

    /**
     * 用于构建资产 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/assets" 前缀。
     */
    public static final String ASSET_URL = PREFIX + "/assets";

    /**
     * 用于构建自定义认证 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/auth" 前缀。
     */
    public static final String CUSTOM_AUTH = PREFIX + "/auth";

    /**
     * 用于构建邀请 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/invitation" 前缀。
     */
    public static final String INVITATION_URL = PREFIX + "/invitation";

    /**
     * 用于构建应用 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/applications" 前缀。
     */
    public static final String APPLICATION_URL = PREFIX + "/applications";

    /**
     * 用于构建应用历史快照 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/application/history-snapshots" 前缀。
     */
    public static final String APPLICATION_HISTORY_URL = PREFIX + "/application/history-snapshots";

    /**
     * 用于构建查询 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/query" 前缀。
     */
    public static final String QUERY_URL = PREFIX + "/query";

    /**
     * 用于构建状态 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/state" 前缀。
     */
    public static final String STATE_URL = PREFIX + "/state";

    /**
     * 用于构建审计日志 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/audit-logs" 前缀。
     */
    public static final String AUDIT_LOG_URL = PREFIX + "/audit-logs";

    /**
     * 用于构建库查询 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/library-queries" 前缀。
     */
    public static final String LIBRARY_QUERY_URL = PREFIX + "/library-queries";

    /**
     * 用于构建库查询记录 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/library-query-records" 前缀。
     */
    public static final String LIBRARY_QUERY_RECORD_URL = PREFIX + "/library-query-records";

    /**
     * 用于构建文件夹 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/folders" 前缀。
     */
    public static final String FOLDER_URL = PREFIX + "/folders";

    /**
     * 用于构建 GitHub 星 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/misc/github-star" 前缀。
     */
    public static final String GITHUB_STAR = PREFIX + "/misc/github-star";

    /**
     * 用于构建 JS 库 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/misc/js-library" 前缀。
     */
    public static final String JS_LIBRARY = PREFIX + "/misc/js-library";

    /**
     * 用于构建物料 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/materials" 前缀。
     */
    public static final String MATERIAL_URL = PREFIX + "/materials";

    /**
     * 用于构建库文件 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/libraries" 前缀。
     */
    public static final String LIBRARY_URL = PREFIX + "/libraries";

    /**
     * 用于构建联系同步 URL 的常量。
     *
     * 该常量用于在 URL 前添加 "/api/sync" 前缀。
     */
    public static final String CONTACT_SYNC = PREFIX + "/sync";
}

package com.barda.sdk.exception;

import static com.barda.sdk.exception.ErrorLogType.SIMPLE;
import static com.barda.sdk.exception.ErrorLogType.VERBOSE;
import static com.barda.sdk.util.EnumUtils.checkDuplicates;
import lombok.Getter;

@Getter
public enum BizError {

    // 5000 - 5100 通用错误代码
    INTERNAL_SERVER_ERROR(500, 5000, VERBOSE), // 服务器内部错误
    NOT_AUTHORIZED(500, 5001), // 未授权
    INVALID_PARAMETER(500, 5002), // 无效参数
    UNSUPPORTED_OPERATION(400, 5003), // 不支持的操作
    DUPLICATE_KEY(409, 5004, VERBOSE), // 重复键
    NO_RESOURCE_FOUND(500, 5005), // 未找到资源
    INFRA_REDIS_TIMEOUT(500, 5006), // 基础设施 Redis 超时
    INFRA_MONGO_TIMEOUT(500, 5007), // 基础设施 MongoDB 超时
    INVALID_PERMISSION_OPERATION(500, 5008), // 无效的权限操作
    REQUEST_THROTTLED(500, 5009), // 请求被限流
    SERVER_NOT_READY(503, 5010), // 服务器未准备就绪
    REDIRECT(302, 5011), // 重定向

    // 组织相关，代码范围 5100 - 5149
    INVALID_ORG_ID(500, 5100), // 无效的组织 ID
    SWITCH_CURRENT_ORG_ERROR(500, 5101), // 切换当前组织错误
    LAST_ADMIN_CANNOT_LEAVE_ORG(500, 5102), // 最后一个管理员不能离开组织
    EXCEED_MAX_USER_ORG_COUNT(500, 5103), // 超出最大用户组织数限制
    EXCEED_MAX_ORG_MEMBER_COUNT(500, 5104), // 超出最大组织成员数限制
    UNABLE_TO_FIND_VALID_ORG(500, 5105), // 无法找到有效的组织
    EXCEED_MAX_DEVELOPER_COUNT(500, 5106), // 超出最大开发者数限制
    ORG_DELETED_FOR_ENTERPRISE_MODE(500, 5107), // 企业模式下组织已删除

    // 组相关，代码范围 5150 - 5199
    INVALID_GROUP_ID(500, 5150), // 无效的群组 ID
    CANNOT_REMOVE_MYSELF(500, 5151), // 无法移除自己
    CANNOT_LEAVE_GROUP(500, 5152), // 无法离开群组
    EXCEED_MAX_GROUP_COUNT(500, 5153), // 超出最大群组数限制
    CANNOT_DELETE_SYSTEM_GROUP(500, 5154), // 不能删除系统群组

    NEED_DEV_TO_CREATE_RESOURCE(500, 5155), // 需要开发者创建资源

    // 邀请相关，代码范围 5200 - 5300
    INVALID_INVITATION_CODE(400, 5200), // 无效的邀请码
    INVITER_NOT_FOUND(404, 5201), // 未找到邀请者
    ALREADY_IN_ORGANIZATION(400, 5202), // 已在组织中
    INVITED_ORG_DELETED(500, 5203), // 被邀请的组织已删除
    INVITED_APPLICATION_DELETED(500, 5204), // 被邀请的应用已删除
    INVITED_USER_NOT_LOGIN(403, 5205), // 被邀请的用户未登录

    // 应用相关，代码范围 5300 - 5400
    QUERY_NOT_FOUND(500, 5300), // 未找到查询
    APPLICATION_NOT_FOUND(500, 5301), // 未找到应用
    ILLEGAL_APPLICATION_PERMISSION_ID(500, 5302), // 非法的应用权限 ID
    EXCEED_MAX_APP_COUNT(500, 5303), // 超出最大应用数限制
    NO_PERMISSION_TO_VIEW(403, 5304), // 没有查看权限

    FETCH_HISTORY_SNAPSHOT_FAILURE(500, 5305), // 获取历史快照失败
    FETCH_HISTORY_SNAPSHOT_COUNT_FAILURE(500, 5306), // 获取历史快照计数失败
    INVALID_HISTORY_SNAPSHOT(500, 5307), // 无效的历史快照

    NO_PERMISSION_TO_REQUEST_APP(403, 5308), // 没有请求应用权限

    // 数据源相关，代码范围 5500 - 5600
    DATASOURCE_NOT_FOUND(500, 5500), // 未找到数据源
    INVALID_DATASOURCE_CONFIGURATION(400, 5501, VERBOSE), // 无效的数据源配置
    DATASOURCE_DELETE_FAIL_DUE_TO_REMAINING_QUERIES(500, 5502), // 由于剩余查询而无法删除数据源
    PLUGIN_CREATE_CONNECTION_FAILED(500, 5503, VERBOSE), // 插件创建连接失败
    DATASOURCE_PLUGIN_ID_NOT_GIVEN(400, 5504), // 未给出数据源插件 ID
    EXCEED_MAX_DATASOURCE_COUNT(500, 5505), // 超出最大数据源数限制
    INVALID_DATASOURCE_CONFIG_TYPE(500, 5506, VERBOSE), // 无效的数据源配置类型
    DATASOURCE_TYPE_ERROR(500, 5507, VERBOSE), // 数据源类型错误
    DUPLICATE_DATABASE_NAME(500, 5508), // 重复的数据库名称
    DATASOURCE_CLOSE_FAILED(500, 5509, VERBOSE), // 数据源关闭失败

    DATASOURCE_AND_APP_ORG_NOT_MATCH(500, 5510), // 数据源和应用组织不匹配
    CERTIFICATE_IS_EMPTY(400, 5511), // 证书为空

    // 与登录相关的代码，代码范围 5600 - 5699
    USER_NOT_SIGNED_IN(401, 5600), // 用户未登录
    FAIL_TO_GET_OIDC_INFO(500, 5601, VERBOSE), // 获取 OIDC 信息失败
    LOG_IN_SOURCE_NOT_SUPPORTED(403, 5602), // 登录来源不支持
    TOO_MANY_REQUESTS(429, 5603), // 请求过多
    INVALID_OTP(403, 5604), // 无效的 OTP

    USER_LOGIN_ID_EXIST(403, 5607), // 用户登录 ID 已存在
    INVALID_PASSWORD(403, 5608), // 无效的密码
    ALREADY_BIND(403, 5609), // 已绑定
    NEED_BIND_THIRD_PARTY_CONNECTION(400, 5610), // 需要绑定第三方连接
    CAS_LOGIN_ERROR(400, 5611), // CAS 登录错误
    DING_TALK_LOGIN_ERROR(400, 5612), // 钉钉登录错误
    CANNOT_FIND_ENTERPRISE_ORG(500, 5613), // 找不到企业组织
    AUTH_ERROR(400, 5614), // 认证错误
    AUTH_REFRESH_ERROR(400, 5615), // 认证刷新错误
    LOGIN_EXPIRED(401, 5616), // 登录已过期
    DISABLE_AUTH_CONFIG_FORBIDDEN(403, 5617), // 禁用认证配置被禁止
    USER_NOT_EXIST(400, 5618), // 用户不存在
    JWT_NOT_FIND(400, 5619), // 未找到 JWT
    ID_NOT_EXIST(500, 5620), // ID 不存在

    // 与资产相关的代码，代码范围 5700 - 5799
    PAYLOAD_TOO_LARGE(413, 5700), // 负载过大

    // 与插件相关的代码，代码范围 5800 - 5899
    PLUGIN_EXECUTION_TIMEOUT(504, 5800), // 插件执行超时
    INVALID_DATASOURCE_TYPE(500, 5801), // 无效的数据源类型
    PLUGIN_EXECUTION_TIMEOUT_WITHOUT_TIME(504, 5802, VERBOSE), // 未指定插件执行超时时间

    // 与业务相关的代码，代码范围 5900 - 5999
    NOT_RELEASE(423, 5901), // 未发布

    // 与模板相关的代码，代码范围 6000 - 6099
    TEMPLATE_NOT_EXIST(500, 6000), // 模板不存在
    TEMPLATE_NOT_CORRECT(500, 6001), // 模板不正确

    // 与查询相关的代码，代码范围 6100 - 6199
    EXCEED_QUERY_REQUEST_SIZE(500, 6100), // 超出查询请求大小限制
    EXCEED_QUERY_RESPONSE_SIZE(500, 6101), // 超出查询响应大小限制
    QUERY_EXECUTION_ERROR(500, 6102), // 查询执行错误
    LIBRARY_QUERY_AND_ORG_NOT_MATCH(400, 6103), // 库查询和组织不匹配
    LIBRARY_QUERY_NOT_FOUND(400, 6104), // 未找到库查询

    // 与用户相关的代码，代码范围 6200 - 6250
    INVALID_USER_STATUS(500, 6200), // 无效的用户状态
    USER_BANNED(500, 6201), // 用户被禁止

    // 与许可证相关的代码，代码范围 6251 - 6300
    CANT_DEPLOY_IN_AIR_GAPPED_ENV(400, 6251), // 无法在空隙环境中部署
    CURRENT_EDITION_NOT_SUPPORT_FOR_THIS_FEATURE(400, 6252), // 当前版本不支持此功能

    // 文件夹相关，代码范围 6301 - 6350
    FOLDER_OPERATE_NO_PERMISSION(500, 6301), // 文件夹操作无权限
    FOLDER_NOT_EXIST(500, 6302), // 文件夹不存在
    FOLDER_NAME_CONFLICT(500, 6303), // 文件夹名称冲突
    ILLEGAL_FOLDER_PERMISSION_ID(500, 6304), // 非法的文件夹权限 ID

    // 素材相关，代码范围 6351 - 6400
    INVALID_MATERIAL_REQUEST(500, 6351) // 无效的素材请求
    ;

    static {
        checkDuplicates(values(), BizError::getBizErrorCode);
    }

    private final int httpErrorCode;
    private final int bizErrorCode;
    private final ErrorLogType errorAction;

    BizError(int httpErrorCode, int bizErrorCode) {
        this(httpErrorCode, bizErrorCode, SIMPLE);
    }

    BizError(int httpErrorCode, int bizErrorCode, ErrorLogType errorLogType) {
        this.httpErrorCode = httpErrorCode;
        this.bizErrorCode = bizErrorCode;
        this.errorAction = errorLogType;
    }

    /**
     * 确定是否记录详细的日志。
     *
     * <p>此方法检查 {@link #errorAction} 的值是否等于 {@link #VERBOSE}。
     * 如果相等，则返回 true，表示需要记录详细的日志；
     * 否则返回 false，表示不需要记录详细的日志。
     *
     * @return 如果 {@link #errorAction} 为 {@link #VERBOSE}，返回 true，否则返回 false
     */
    public boolean logVerbose() {
        return this.errorAction == VERBOSE;
    }
}

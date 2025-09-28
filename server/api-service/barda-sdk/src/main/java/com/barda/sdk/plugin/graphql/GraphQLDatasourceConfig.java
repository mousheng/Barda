package com.barda.sdk.plugin.graphql;

import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_CONFIG_TYPE;
import static com.barda.sdk.util.ExceptionUtils.ofException;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;
import static com.barda.sdk.util.JsonUtils.fromJson;
import static com.barda.sdk.util.JsonUtils.toJson;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.collections4.SetUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.barda.sdk.exception.PluginCommonError;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.Property;
import com.barda.sdk.plugin.restapi.auth.AuthConfig;
import com.barda.sdk.plugin.restapi.auth.RestApiAuthType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 表示GraphQL数据源配置类，包含连接参数、请求体、表单数据、头信息、URL及认证配置等。
 */
@Builder
public class GraphQLDatasourceConfig implements DatasourceConnectionConfig {

    /**
     * 空配置常量，代表一个空的GraphQL数据源配置。
     */
    public static final GraphQLDatasourceConfig EMPTY_CONFIG = GraphQLDatasourceConfig.builder().build();

    /**
     * 请求参数列表。
     */
    private final List<Property> params;

    /**
     * 请求体内容。
     */
    private final String body;

    /**
     * 表单数据列表。
     */
    private final List<Property> bodyFormData;

    /**
     * 请求头信息列表。
     */
    private final List<Property> headers;

    /**
     * 请求URL。
     */
    private final String url;

    /**
     * 转发的Cookie集合。
     */
    private final Set<String> forwardCookies;

    /**
     * 是否转发所有Cookie。
     */
    private final boolean forwardAllCookies;

    /**
     * 认证配置信息。
     */
    @Getter
    @Setter
    @Nullable
    private AuthConfig authConfig;

    /**
     * 使用指定的参数构造GraphQL数据源配置对象。
     *
     * @param params           请求参数列表
     * @param body             请求体内容
     * @param bodyFormData     表单数据列表
     * @param headers          请求头信息列表
     * @param url              请求URL
     * @param forwardCookies   转发的Cookie集合
     * @param forwardAllCookies 是否转发所有Cookie
     * @param authConfig       认证配置信息
     */
    @JsonCreator
    private GraphQLDatasourceConfig(List<Property> params, String body, List<Property> bodyFormData, List<Property> headers,
            String url, Set<String> forwardCookies, boolean forwardAllCookies, @Nullable AuthConfig authConfig) {
        this.params = params;
        this.body = body;
        this.bodyFormData = bodyFormData;
        this.headers = headers;
        this.url = url;
        this.forwardCookies = forwardCookies;
        this.forwardAllCookies = forwardAllCookies;
        this.authConfig = authConfig;
    }

    /**
     * 从请求映射构建GraphQL数据源配置对象。
     *
     * @param requestMap 请求映射
     * @return 构建的GraphQL数据源配置对象
     * @throws PluginException 如果配置无效，抛出插件异常
     */
    public static GraphQLDatasourceConfig buildFrom(Map<String, Object> requestMap) {
        GraphQLDatasourceConfig result = fromJson(toJson(requestMap), GraphQLDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(PluginCommonError.DATASOURCE_ARGUMENT_ERROR, "INVALID_RESTAPI_CONFIG");
        }
        return result;
    }

    /**
     * 从数据源连接配置创建GraphQL数据源配置对象。
     *
     * @param datasourceConfig 数据源连接配置
     * @return 构建的GraphQL数据源配置对象，如果类型不匹配则返回null
     */
    public static GraphQLDatasourceConfig from(DatasourceConnectionConfig datasourceConfig) {

        if (datasourceConfig instanceof GraphQLDatasourceConfig config) {
            return config;
        }

        return null;
    }

    /**
     * 获取请求头信息列表。
     *
     * @return 请求头信息列表
     */
    public List<Property> getHeaders() {
        return emptyIfNull(headers);
    }

    /**
     * 获取请求参数列表。
     *
     * @return 请求参数列表
     */
    public List<Property> getParams() {
        return emptyIfNull(params);
    }

    /**
     * 获取表单数据列表。
     *
     * @return 表单数据列表
     */
    public List<Property> getBodyFormData() {
        return emptyIfNull(bodyFormData);
    }

    /**
     * 获取请求体内容。
     *
     * @return 请求体内容
     */
    public String getBody() {
        return body;
    }

    /**
     * 获取请求URL。
     *
     * @return 请求URL
     */
    public String getUrl() {
        return trimToEmpty(url);
    }

    /**
     * 获取认证类型。
     *
     * @return 认证类型
     */
    public RestApiAuthType getAuthType() {
        if (this.authConfig != null) {
            return this.authConfig.getType();
        }
        // 默认返回无认证类型
        return RestApiAuthType.NO_AUTH;
    }

    /**
     * 获取转发的Cookie集合。
     *
     * @return 转发的Cookie集合
     */
    public Set<String> getForwardCookies() {
        return SetUtils.emptyIfNull(forwardCookies);
    }

    /**
     * 检查是否转发所有Cookie。
     *
     * @return 如果转发所有Cookie返回true，否则返回false
     */
    public boolean isForwardAllCookies() {
        return forwardAllCookies;
    }

    /**
     * 合并更新后的配置并返回新的数据源连接配置。
     *
     * @param updatedConfig 更新后的数据源连接配置
     * @return 合并后的数据源连接配置
     * @throws PluginException 如果配置类型无效，抛出插件异常
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig updatedConfig) {
        if (!(updatedConfig instanceof GraphQLDatasourceConfig updatedApiConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE", updatedConfig.getClass().getSimpleName());
        }
        if (this.authConfig != null) {
            updatedApiConfig.setAuthConfig(this.authConfig.mergeWithUpdatedConfig(updatedApiConfig.getAuthConfig()));
        }
        return updatedApiConfig;
    }

    /**
     * 对配置中的敏感信息进行加密处理。
     *
     * @param encryptFunc 加密函数
     * @return 加密后的数据源连接配置
     */
    @Override
    public DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        if (this.authConfig != null) {
            this.authConfig.doEncrypt(encryptFunc);
        }
        return this;
    }

    /**
     * 对配置中的敏感信息进行解密处理。
     *
     * @param decryptFunc 解密函数
     * @return 解密后的数据源连接配置
     */
    @Override
    public DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        if (this.authConfig != null) {
            this.authConfig.doDecrypt(decryptFunc);
        }
        return this;
    }
}

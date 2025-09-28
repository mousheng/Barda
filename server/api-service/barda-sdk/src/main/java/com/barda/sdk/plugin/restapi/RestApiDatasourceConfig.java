package com.barda.sdk.plugin.restapi;

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
import com.barda.sdk.plugin.common.ssl.SslConfig;
import com.barda.sdk.plugin.restapi.auth.AuthConfig;
import com.barda.sdk.plugin.restapi.auth.RestApiAuthType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 基于Builder模式的REST API数据源配置类。
 * 该类实现了DatasourceConnectionConfig接口，用于配置REST API数据源。
 */
@Builder
public class RestApiDatasourceConfig implements DatasourceConnectionConfig {

    /**
     * 空的RestApiDatasourceConfig实例，用于表示未提供配置。
     */
    public static final RestApiDatasourceConfig EMPTY_CONFIG = RestApiDatasourceConfig.builder().build();

    /**
     * 请求参数列表。
     */
    private final List<Property> params;

    /**
     * 请求正文。
     */
    private final String body;

    /**
     * 表单数据参数列表。
     */
    private final List<Property> bodyFormData;

    /**
     * 请求头部列表。
     */
    private final List<Property> headers;

    /**
     * REST API的URL。
     */
    private final String url;

    /**
     * 要转发的Cookie列表。
     */
    private final Set<String> forwardCookies;

    /**
     * 是否转发所有Cookie。
     */
    private final boolean forwardAllCookies;

    /**
     * 认证配置。
     */
    @Getter
    @Setter
    @Nullable
    private AuthConfig authConfig;

    /**
     * SSL配置。
     */
    @Getter
    @Setter
    private SslConfig sslConfig;

    /**
     * 私有构造函数，用于通过Builder模式创建RestApiDatasourceConfig实例。
     */
    @JsonCreator
    private RestApiDatasourceConfig(List<Property> params, String body, List<Property> bodyFormData, List<Property> headers,
            String url, Set<String> forwardCookies, boolean forwardAllCookies, @Nullable AuthConfig authConfig, SslConfig sslConfig) {
        this.params = params;
        this.body = body;
        this.bodyFormData = bodyFormData;
        this.headers = headers;
        this.url = url;
        this.forwardCookies = forwardCookies;
        this.forwardAllCookies = forwardAllCookies;
        this.authConfig = authConfig;
        this.sslConfig = sslConfig;
    }

    /**
     * 从Map中构建RestApiDatasourceConfig实例。
     *
     * @param requestMap 包含配置信息的Map
     * @return 构建的RestApiDatasourceConfig实例
     * @throws PluginException 如果Map中包含无效的配置
     */
    public static RestApiDatasourceConfig buildFrom(Map<String, Object> requestMap) {
        RestApiDatasourceConfig result = fromJson(toJson(requestMap), RestApiDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(PluginCommonError.DATASOURCE_ARGUMENT_ERROR, "INVALID_RESTAPI_CONFIG");
        }
        return result;
    }

    /**
     * 从DatasourceConnectionConfig中转换为RestApiDatasourceConfig。
     *
     * @param datasourceConfig 原始的DatasourceConnectionConfig
     * @return 转换后的RestApiDatasourceConfig，如果原始的DatasourceConnectionConfig不是RestApiDatasourceConfig，返回null
     */
    public static RestApiDatasourceConfig from(DatasourceConnectionConfig datasourceConfig) {

        if (datasourceConfig instanceof RestApiDatasourceConfig config) {
            return config;
        }

        return null;
    }

    /**
     * 获取请求头部列表。
     *
     * @return 请求头部列表，如果为空，返回空列表
     */
    public List<Property> getHeaders() {
        return emptyIfNull(headers);
    }

    /**
     * 获取请求参数列表。
     *
     * @return 请求参数列表，如果为空，返回空列表
     */
    public List<Property> getParams() {
        return emptyIfNull(params);
    }

    /**
     * 获取表单数据参数列表。
     *
     * @return 表单数据参数列表，如果为空，返回空列表
     */
    public List<Property> getBodyFormData() {
        return emptyIfNull(bodyFormData);
    }

    /**
     * 获取请求正文。
     *
     * @return 请求正文
     */
    public String getBody() {
        return body;
    }

    /**
     * 获取REST API的URL。
     *
     * @return REST API的URL
     */
    public String getUrl() {
        return trimToEmpty(url);
    }

    /**
     * 获取认证类型。
     *
     * @return 认证类型，如果未配置认证，返回RestApiAuthType.NO_AUTH
     */
    public RestApiAuthType getAuthType() {
        if (this.authConfig != null) {
            return this.authConfig.getType();
        }
        //default
        return RestApiAuthType.NO_AUTH;
    }

    /**
     * 获取要转发的Cookie列表。
     *
     * @return 要转发的Cookie列表，如果为空，返回空集合
     */
    public Set<String> getForwardCookies() {
        return SetUtils.emptyIfNull(forwardCookies);
    }

    /**
     * 获取是否转发所有Cookie。
     *
     * @return 是否转发所有Cookie
     */
    public boolean isForwardAllCookies() {
        return forwardAllCookies;
    }

    /**
     * 合并当前的RestApiDatasourceConfig和更新的DatasourceConnectionConfig。
     *
     * @param updatedConfig 要合并的更新的DatasourceConnectionConfig
     * @return 合并后的DatasourceConnectionConfig
     * @throws PluginException 如果更新的DatasourceConnectionConfig不是RestApiDatasourceConfig
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig updatedConfig) {
        if (!(updatedConfig instanceof RestApiDatasourceConfig updatedApiConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE", updatedConfig.getClass().getSimpleName());
        }
        if (this.authConfig != null) {
            updatedApiConfig.setAuthConfig(this.authConfig.mergeWithUpdatedConfig(updatedApiConfig.getAuthConfig()));
        }
        if (this.sslConfig != null) {
            updatedApiConfig.setSslConfig(this.sslConfig.mergeWithUpdatedConfig(updatedApiConfig.getSslConfig()));
        }
        return updatedApiConfig;
    }

    /**
     * 对RestApiDatasourceConfig中的敏感信息进行加密。
     *
     * @param encryptFunc 用于加密的函数
     * @return 加密后的RestApiDatasourceConfig
     */
    @Override
    public DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        if (this.authConfig != null) {
            this.authConfig.doEncrypt(encryptFunc);
        }
        if (this.sslConfig != null) {
            this.sslConfig.doEncrypt(encryptFunc);
        }
        return this;
    }

    /**
     * 对RestApiDatasourceConfig中的敏感信息进行解密。
     *
     * @param decryptFunc 用于解密的函数
     * @return 解密后的RestApiDatasourceConfig
     */
    @Override
    public DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        if (this.authConfig != null) {
            this.authConfig.doDecrypt(decryptFunc);
        }
        if (this.sslConfig != null) {
            this.sslConfig.doDecrypt(decryptFunc);
        }
        return this;
    }
}

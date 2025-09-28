package com.barda.plugin.redis.model;

import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_CONFIG_TYPE;
import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_ARGUMENT_ERROR;
import static com.barda.sdk.util.ExceptionUtils.ofException;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;
import static com.barda.sdk.util.JsonUtils.fromJson;
import static com.barda.sdk.util.JsonUtils.toJson;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonView;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.models.DatasourceConnectionConfig;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 定义了 Redis 数据源的配置信息。
 * 实现了 DatasourceConnectionConfig 接口，提供对数据源配置的操作。
 */
@Slf4j
@Getter
@Builder
public class RedisDatasourceConfig implements DatasourceConnectionConfig {

    /**
     * Redis 主机地址。
     */
    private final String host;

    /**
     * Redis 端口号。
     */
    private final Long port;

    /**
     * 是否使用 SSL 连接。
     */
    private final boolean usingSsl;

    /**
     * Redis 用户名。
     */
    private final String username;

    /**
     * Redis 密码。
     * 标记为 JsonView(JsonViews.Internal.class) 仅在内部视图中可见。
     */
    @JsonView(JsonViews.Internal.class)
    private String password;

    /**
     * 是否使用 URI 连接。
     */
    private final boolean usingUri;

    /**
     * Redis URI。
     * 标记为 JsonView(JsonViews.Internal.class) 仅在内部视图中可见。
     */
    @JsonView(JsonViews.Internal.class)
    private String uri;

    /**
     * 构造函数。
     * 用于 JSON 反序列化。
     */
    @JsonCreator
    public RedisDatasourceConfig(String host, Long port, boolean usingSsl, String username, String password, boolean usingUri, String uri) {
        this.host = host;
        this.port = port;
        this.usingSsl = usingSsl;
        this.username = username;
        this.password = password;
        this.usingUri = usingUri;
        this.uri = uri;
    }

    /**
     * 从 Map 构建 RedisDatasourceConfig 对象。
     * 如果构建失败，将抛出 PluginException。
     */
    public static RedisDatasourceConfig buildFrom(Map<String, Object> requestMap) {
        RedisDatasourceConfig result = fromJson(toJson(requestMap), RedisDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(DATASOURCE_ARGUMENT_ERROR, "INVALID_REDIS_CONFIG");
        }
        return result;
    }

    /**
     * 加密 Redis 密码和 URI。
     * 若发生异常，将在 log 中记录并返回未加密的对象。
     */
    @Override
    public DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        try {
            password = encryptFunc.apply(password);
            uri = encryptFunc.apply(uri);
            return this;
        } catch (Exception e) {
            log.error("fail to encrypt password: {}", password, e);
            return this;
        }
    }

    /**
     * 解密 Redis 密码和 URI。
     * 若发生异常，将在 log 中记录并返回未解密的对象。
     */
    @Override
    public DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        try {
            password = decryptFunc.apply(password);
            uri = decryptFunc.apply(uri);
            return this;
        } catch (Exception e) {
            log.error("fail to encrypt password: {}", password, e);
            return this;
        }
    }

    /**
     * 合并更新的配置信息。
     * 如果更新的配置不是 RedisDatasourceConfig 类型，将抛出 Exception。
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig updatedConfig) {

        if (!(updatedConfig instanceof RedisDatasourceConfig updatedRedisConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE", updatedConfig.getClass().getSimpleName());
        }

        if (updatedRedisConfig.isUsingUri()) {
            return RedisDatasourceConfig.builder()
                    .usingUri(true)
                    .uri(firstNonNull(updatedRedisConfig.getUri(), getUri()))
                    .build();
        }

        return RedisDatasourceConfig.builder()
                .usingUri(false)
                .usingSsl(updatedRedisConfig.isUsingSsl())
                .host(updatedRedisConfig.getHost())
                .port(updatedRedisConfig.getPort())
                .username(updatedRedisConfig.getUsername())
                .password(firstNonNull(updatedRedisConfig.getPassword(), this.getPassword()))
                .build();
    }
}

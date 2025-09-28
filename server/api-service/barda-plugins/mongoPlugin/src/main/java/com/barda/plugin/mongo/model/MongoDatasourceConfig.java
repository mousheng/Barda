package com.barda.plugin.mongo.model;

import static com.barda.plugin.mongo.model.MongoConnectionUriParser.parseDatabaseFrom;
import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_CONFIG_TYPE;
import static com.barda.sdk.util.ExceptionUtils.ofException;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;
import static com.barda.sdk.util.JsonUtils.fromJson;
import static com.barda.sdk.util.JsonUtils.toJson;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.exception.PluginCommonError;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.Endpoint;

import lombok.Builder;
import lombok.Getter;

/**
 * 用于配置 MongoDB 数据源的类。实现了 {@link DatasourceConnectionConfig} 接口。
 * 该类使用 Lombok 库提供的 @Getter 和 @Builder 注解来生成 getter 方法和 builder 模式的构造器。
 */
@Getter
@Builder
public class MongoDatasourceConfig implements DatasourceConnectionConfig {

    /**
     * 指示是否使用 URI 进行连接。
     */
    private final boolean usingUri;

    /**
     * 指示是否使用 SRV 模式进行连接。
     */
    private final boolean srvMode;

    /**
     * 指示是否使用 SSL 进行连接。
     */
    private final boolean ssl;

    /**
     * MongoDB 连接 URI。
     * 仅在 usingUri 为 true 时使用。
     */
    @JsonView(JsonViews.Internal.class)
    private String uri;

    /**
     * MongoDB 集群的端点列表。
     * 仅在 usingUri 为 false 时使用。
     */
    private final List<Endpoint> endpoints;

    /**
     * MongoDB 主机名。
     * 仅在 usingUri 为 false 时使用。
     */
    private final String host;

    /**
     * MongoDB 端口。
     * 仅在 usingUri 为 false 时使用。
     */
    private final Integer port;

    /**
     * MongoDB 数据库名称。
     * 仅在 usingUri 为 false 时使用。
     */
    private final String database;

    /**
     * MongoDB 验证账号密码的数据库，默认为admin
     */
    private final String authSource;
    /**
     * MongoDB 用户名。
     */
    private final String username;

    /**
     * MongoDB 密码。
     * 仅在 usingUri 为 false 时使用。
     */
    @JsonView(JsonViews.Internal.class)
    private String password;

    /**
     * MongoDB 认证机制。
     */
    private final MongoAuthMechanism authMechanism;

    /**
     * 私有构造器，使用 Lombok 库提供的 @Builder 注解生成。
     */
    @JsonCreator
    private MongoDatasourceConfig(boolean usingUri, boolean srvMode,
                                  boolean ssl, String uri, List<Endpoint> endpoints,
                                  String host, Integer port, String database, String username, String authSource, String password,
                                  MongoAuthMechanism authMechanism) {
        this.usingUri = usingUri;
        this.srvMode = srvMode;
        this.ssl = ssl;
        this.uri = uri;
        this.endpoints = endpoints;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.authSource = authSource;
        this.password = password;
        this.authMechanism = authMechanism;
    }

    /**
     * 从 Map 构建 MongoDatasourceConfig 对象。
     *
     * @param requestMap 包含配置信息的 Map
     * @return 构建的 MongoDatasourceConfig 对象
     * @throws PluginException 如果 Map 包含无效的 MongoDB 配置
     */
    public static MongoDatasourceConfig buildFrom(Map<String, Object> requestMap) {
        MongoDatasourceConfig result = fromJson(toJson(requestMap), MongoDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(PluginCommonError.DATASOURCE_ARGUMENT_ERROR, "INVALID_MONGODB_CONFIG");
        }
        return result;
    }

    /**
     * 获取用户名，并去掉首尾空格。
     *
     * @return 去掉首尾空格的用户名
     */
    public String getUsername() {
        return StringUtils.trimToEmpty(username);
    }

    /**
     * 合并当前的 MongoDatasourceConfig 对象和另一个 MongoDatasourceConfig 对象，
     * 并返回一个新的 MongoDatasourceConfig 对象。
     *
     * @param updatedConfig 要合并的 MongoDatasourceConfig 对象
     * @return 合并后的 MongoDatasourceConfig 对象
     * @throws PluginException 如果 updatedConfig 不是 MongoDatasourceConfig 类型
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig updatedConfig) {

        if (!(updatedConfig instanceof MongoDatasourceConfig updatedMongoConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE", updatedConfig.getClass().getSimpleName());
        }

        if (updatedMongoConfig.isUsingUri()) {
            return MongoDatasourceConfig.builder()
                    .usingUri(true)
                    .uri(firstNonNull(updatedMongoConfig.getUri(), getUri()))
                    .build();
        }

        return MongoDatasourceConfig.builder()
                .usingUri(false)
                .srvMode(updatedMongoConfig.isSrvMode())
                .ssl(updatedMongoConfig.isSsl())
                .authSource(updatedMongoConfig.getAuthSource())
                .authMechanism(updatedMongoConfig.getAuthMechanism())
                .endpoints(updatedMongoConfig.getEndpoints())
                .database(updatedMongoConfig.getDatabase())
                .username(updatedMongoConfig.getUsername())
                .password(firstNonNull(updatedMongoConfig.getPassword(), this.getPassword()))
                .host(updatedMongoConfig.getHost())
                .port(updatedMongoConfig.getPort())
                .build();
    }

    /**
     * 获取 MongoDatasourceConfig 的 builder 对象。
     * 该方法使用 Lombok 库提供的 @Builder 注解生成。
     *
     * @return MongoDatasourceConfig 的 builder 对象
     */
    @VisibleForTesting
    public MongoDatasourceConfigBuilder toBuilder() {
        return builder()
                .usingUri(usingUri)
                .uri(uri)
                .srvMode(srvMode)
                .ssl(ssl)
                .authSource(authSource)
                .authMechanism(authMechanism)
                .endpoints(endpoints)
                .database(database)
                .username(username)
                .password(password)
                .host(host)
                .port(port);
    }

    /**
     * 对 MongoDatasourceConfig 对象中的敏感信息进行加密。
     *
     * @param encryptFunc 用于加密的函数
     * @return 加密后的 MongoDatasourceConfig 对象
     */
    @Override
    public DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        password = encryptFunc.apply(password);
        uri = encryptFunc.apply(uri);
        return this;
    }

    /**
     * 对 MongoDatasourceConfig 对象中的敏感信息进行解密。
     *
     * @param decryptFunc 用于解密的函数
     * @return 解密后的 MongoDatasourceConfig 对象
     */
    @Override
    public DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        password = decryptFunc.apply(password);
        uri = decryptFunc.apply(uri);
        return this;
    }

    /**
     * 获取 MongoDB 集群的端点列表。
     *
     * @return MongoDB 集群的端点列表
     */
    public List<Endpoint> getEndpoints() {
        return ListUtils.emptyIfNull(endpoints);
    }

    /**
     * 获取 MongoDB 端口。
     * 如果 port 为 null，返回 27017。
     *
     * @return MongoDB 端口
     */
    public int getPort() {
        return port == null ? 27017 : port;
    }

    /**
     * 获取 MongoDB 数据库名称。
     * 如果 usingUri 为 true，从 URI 中解析出数据库名称。
     *
     * @return MongoDB 数据库名称
     */
    @JsonIgnore
    public String getParsedDatabase() {
        if (usingUri) {
            return parseDatabaseFrom(uri);
        }
        return database;
    }
}

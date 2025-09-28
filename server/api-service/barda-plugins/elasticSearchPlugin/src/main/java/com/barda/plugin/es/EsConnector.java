package com.barda.plugin.es;

import static com.barda.sdk.exception.BizError.DATASOURCE_CLOSE_FAILED;
import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_ARGUMENT_ERROR;
import static com.barda.sdk.util.ExceptionUtils.ofException;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.pf4j.Extension;
import org.springframework.http.HttpMethod;

import com.google.common.base.Joiner;
import com.barda.plugin.es.model.EsConnection;
import com.barda.plugin.es.model.EsDatasourceConfig;
import com.barda.sdk.config.CommonConfig;
import com.barda.sdk.config.dynamic.Conf;
import com.barda.sdk.config.dynamic.ConfigCenter;
import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.plugin.common.DatasourceConnector;
import com.barda.sdk.plugin.common.QueryExecutionUtils;
import com.barda.sdk.util.ExceptionUtils;
import com.barda.sdk.util.JsonUtils;
import com.barda.sdk.util.Preconditions;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Elasticsearch数据源连接器。
 *
 * <p>
 * 文档参考：
 * restClient: <a href="https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/java-rest-low.html">...</a>
 * auth: <a href="https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/_basic_authentication.html">...</a>
 * </p>
 */
@Slf4j
@Extension
public class EsConnector implements DatasourceConnector<EsConnection, EsDatasourceConfig> {

    private static final Joiner JOINER = Joiner.on("/");

    private final Conf<Duration> datasourceValidateTimeout;
    private final CommonConfig commonConfig;

    /**
     * 构造器。
     *
     * @param configCenter 配置中心
     * @param commonConfig 通用配置
     */
    public EsConnector(ConfigCenter configCenter, CommonConfig commonConfig) {
        datasourceValidateTimeout = configCenter.mongoPlugin().ofInteger("datasourceValidateTimeoutMillis", 6000)
                .then(Duration::ofMillis);
        this.commonConfig = commonConfig;
    }

    /**
     * 解析数据源配置。
     *
     * @param configMap 配置Map
     * @return 解析后的EsDatasourceConfig
     */
    @Nonnull
    @Override
    public EsDatasourceConfig resolveConfig(Map<String, Object> configMap) {
        EsDatasourceConfig result = JsonUtils.fromJson(JsonUtils.toJson(configMap), EsDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(DATASOURCE_ARGUMENT_ERROR, "INVALID_ES_CONFIG");
        }
        return result;
    }

    /**
     * 验证数据源配置。
     *
     * @param connectionConfig 连接配置
     * @return 验证不通过的项
     */
    @Override
    public Set<String> validateConfig(EsDatasourceConfig connectionConfig) {
        Set<String> invalids = new HashSet<>();
        if (StringUtils.isBlank(connectionConfig.getConnectionString())) {
            invalids.add("CONNECTION_STRING_EMPTY");
        }
        return invalids;
    }

    /**
     * 创建数据源连接。
     *
     * @param connectionConfig 连接配置
     * @return 连接Mono
     */
    @Override
    public Mono<EsConnection> createConnection(EsDatasourceConfig connectionConfig) {
        return Mono.fromSupplier(() -> buildRestClient(connectionConfig))
                .map(ReactorRestClientAdaptor::new)
                .map(EsConnection::new)
                .subscribeOn(QueryExecutionUtils.querySharedScheduler());
    }

    /**
     * 创建低级REST客户端。
     *
     * <p>
     * 注意：此方法是阻塞的！
     * </p>
     *
     * @param esDatasourceConfig Es数据源配置
     * @return RestClient
     */
    private RestClient buildRestClient(EsDatasourceConfig esDatasourceConfig) {
        ConnectionStringParseResult parseResult = parseConnectionString(esDatasourceConfig.getConnectionString());
        if (commonConfig.getDisallowedHosts().contains(parseResult.getHost())) {
            throw new BizException(BizError.INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_CONNECTION_STRING");
        }
        HttpHost httpHost = new HttpHost(parseResult.getHost(), parseResult.getPort(), parseResult.getSchema());

        RestClientBuilder restClientBuilder = RestClient.builder(httpHost);
        // 身份验证
        if (StringUtils.isNotBlank(esDatasourceConfig.getUsername()) && StringUtils.isNotBlank(esDatasourceConfig.getPassword())) {
            UsernamePasswordCredentials usernamePasswordCredentials =
                    new UsernamePasswordCredentials(esDatasourceConfig.getUsername(), esDatasourceConfig.getPassword());
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, usernamePasswordCredentials);
            restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        // https://github.com/elastic/elasticsearch/issues/71026
        // restClient在连接到80(443)主机时会生成一个Header "Host: {host}:{defaultPort}"，
        // 而es服务器会返回一个"no such host"错误。 要解决这个问题，我们在这里覆盖Host头。
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Host
        if (parseResult.getPort() == -1) {
            restClientBuilder.setDefaultHeaders(new Header[] {new BasicHeader("Host", parseResult.getHost())});
        }

        // 前缀
        if (StringUtils.isNotBlank(parseResult.getPrefix())) {
            restClientBuilder.setPathPrefix("/" + parseResult.getPrefix());
        }
        return restClientBuilder.build();
    }

    /**
     * 销毁数据源连接。
     *
     * @param esConnection Es连接
     * @return 销毁Mono
     */
    @Override
    public Mono<Void> destroyConnection(EsConnection esConnection) {
        return Mono.<Void> fromRunnable(() ->
                        Optional.ofNullable(esConnection)
                                .ifPresent(connection -> {
                                    try {
                                        connection.close();
                                    } catch (IOException e) {
                                        throw ofException(DATASOURCE_CLOSE_FAILED, "DATASOURCE_CLOSE_FAILED", e.getMessage());
                                    }
                                }))
                .subscribeOn(QueryExecutionUtils.querySharedScheduler());
    }

    /**
     * 测试数据源连接。
     *
     * @param connectionConfig 连接配置
     * @return 测试结果Mono
     */
    @Override
    public Mono<DatasourceTestResult> testConnection(EsDatasourceConfig connectionConfig) {
        return Mono.from(doCreateConnection(connectionConfig))
                .zipWhen(esConnection -> {
                    Request request = new Request(HttpMethod.HEAD.name(), "");
                    return esConnection.reactorRestClientAdaptor().request(request);
                })
                .timeout(datasourceValidateTimeout.get())
                .map(zip -> {
                    // 关闭
                    try {
                        zip.getT1().close();
                    } catch (IOException e) {
                        log.error("关闭rest client出错。", e);
                    }
                    // 解析响应
                    Response response = zip.getT2();
                    if (response.getStatusLine().getStatusCode() == 200) {
                        return DatasourceTestResult.testSuccess();
                    }

                    log.error("测试es失败。{},{}", JsonUtils.toJson(connectionConfig), response);
                    return DatasourceTestResult.testFail(response.getStatusLine().getReasonPhrase());
                })
                .onErrorResume(throwable -> {
                    log.error("测试es出错。{}", JsonUtils.toJson(connectionConfig), throwable);
                    return Mono.just(DatasourceTestResult.testFail(throwable));
                })
                .subscribeOn(QueryExecutionUtils.querySharedScheduler());
    }

    /**
     * 解析连接字符串。
     *
     * @param connectionString 连接字符串
     * @return 解析结果
     */
    private ConnectionStringParseResult parseConnectionString(String connectionString) {

        ConnectionStringParseResult parseResult = new ConnectionStringParseResult();

        String[] connectionStrings = connectionString.split("://");
        Preconditions.check(connectionStrings.length == 1 || connectionStrings.length == 2, BizError.INVALID_DATASOURCE_CONFIG_TYPE,
                "INVALID_CONNECTION_STRING");
        // schema
        if (connectionStrings.length == 2) {
            parseResult.setSchema(connectionStrings[0].trim());
        }

        String hostPortAndPrefixString = connectionStrings[connectionStrings.length - 1];
        String[] hostPortAndPrefixes = hostPortAndPrefixString.split("/");
        // host & port
        String hostPortString = hostPortAndPrefixes[0];
        String[] hostPorts = hostPortString.split(":");

        Preconditions.check(hostPorts.length == 1 || hostPorts.length == 2, BizError.INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_CONNECTION_STRING");
        // host
        parseResult.setHost(hostPorts[0].trim());

        // connectionString可能不包含端口
        if (hostPorts.length == 2) {
            try {
                // port
                parseResult.setPort(Integer.parseInt(hostPorts[1].trim()));
            } catch (NumberFormatException e) {
                log.error("端口解析出错。{}", connectionString, e);
                throw ExceptionUtils.ofException(BizError.INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_CONNECTION_STRING");
            }
        }
        // 前缀
        if (hostPortAndPrefixes.length > 1) {
            List<String> prefixes = Arrays.stream(hostPortAndPrefixes)
                    .map(String::trim)
                    .toList()
                    .subList(1, hostPortAndPrefixes.length);
            parseResult.setPrefix(JOINER.join(prefixes));
        }
        return parseResult;
    }

    /**
 * 连接字符串解析结果
 */
    private static class ConnectionStringParseResult {

    /**
     * 协议
     * 默认为 HTTP
     */
        private String schema = "http";

    /**
     * 主机
     */
        private String host;

    /**
     * 端口
     * 默认为 -1，表示未指定
     */
        private int port = -1;

    /**
     * 前缀
     * 默认为空
     */
    private String prefix = "";

    /**
     * 获取协议
     *
     * @return 协议
     */
        public String getSchema() {
            return schema;
        }

    /**
     * 设置协议
     *
     * @param schema 要设置的协议
     */
        public void setSchema(String schema) {
            this.schema = schema;
        }

    /**
     * 获取主机
     *
     * @return 主机
     */
        public String getHost() {
            return host;
        }

    /**
     * 设置主机
     *
     * @param host 要设置的主机
     */
        public void setHost(String host) {
            this.host = host;
        }

    /**
     * 获取端口
     *
     * @return 端口
     */
        public int getPort() {
            return port;
        }

    /**
     * 设置端口
     *
     * @param port 要设置的端口
     */
        public void setPort(int port) {
            this.port = port;
        }

    /**
     * 获取前缀
     *
     * @return 前缀
     */
        public String getPrefix() {
            return prefix;
        }

    /**
     * 设置前缀
     *
     * @param prefix 要设置的前缀
     */
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }
}

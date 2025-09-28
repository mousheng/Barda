package com.barda.plugin.restapi;

import static java.util.Collections.emptySet;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.pf4j.Extension;

import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.plugin.common.DatasourceConnector;
import com.barda.sdk.plugin.restapi.RestApiDatasourceConfig;

import reactor.core.publisher.Mono;

/**
 * 实现了 {@link DatasourceConnector} 接口，用于与 REST API 进行数据源连接。
 */
@Extension
public class RestApiConnector implements DatasourceConnector<Object, RestApiDatasourceConfig> {

    /**
     * 创建与 REST API 的连接。
     *
     * @param connectionConfig 连接配置
     * @return 连接对象
     */
    @Override
    public Mono<Object> createConnection(RestApiDatasourceConfig connectionConfig) {
        return Mono.just(new Object());
    }

    /**
     * 验证连接配置是否正确。
     *
     * @param connectionConfig 连接配置
     * @return 验证结果，如果为空，表示配置正确
     */
    @Override
    public Set<String> validateConfig(RestApiDatasourceConfig connectionConfig) {
        return emptySet();
    }

    /**
     * 测试与 REST API 的连接是否正常。
     *
     * @param connectionConfig 连接配置
     * @return 测试结果
     */
    @Override
    public Mono<DatasourceTestResult> testConnection(RestApiDatasourceConfig connectionConfig) {
        return Mono.just(DatasourceTestResult.testSuccess());
    }

    /**
     * 销毁与 REST API 的连接。
     *
     * @param connection 连接对象
     * @return 空的 Mono
     */
    @Override
    public Mono<Void> destroyConnection(Object connection) {
        return Mono.empty();
    }

    /**
     * 从 Map 中解析出 {@link RestApiDatasourceConfig}。
     *
     * @param configMap 配置 Map
     * @return 解析出的 {@link RestApiDatasourceConfig}
     */
    @Nonnull
    @Override
    public RestApiDatasourceConfig resolveConfig(Map<String, Object> configMap) {
        return RestApiDatasourceConfig.buildFrom(configMap);
    }

}
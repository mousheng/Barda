package com.barda.plugin;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.pf4j.Extension;

import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.plugin.common.DatasourceConnector;
import com.barda.sdk.plugin.bardaapi.BardaApiDatasourceConfig;

import reactor.core.publisher.Mono;

/**
 * 实现了 DatasourceConnector 接口的 BardaApiConnector 类。
 * 该类用于连接和测试与 Barda API 的数据源。
 */
@Extension
public class BardaApiConnector implements DatasourceConnector<Object, BardaApiDatasourceConfig> {

    private static final Object CONNECTION_OBJECT = new Object();

    /**
     * 创建与 Barda API 的数据源的连接。
     *
     * @param connectionConfig 连接配置
     * @return 连接对象
     */
    @Override
    public Mono<Object> createConnection(BardaApiDatasourceConfig connectionConfig) {
        return Mono.just(CONNECTION_OBJECT);
    }

    /**
     * 销毁与 Barda API 的数据源的连接。
     *
     * @param o 连接对象
     * @return 空的 Mono
     */
    @Override
    public Mono<Void> destroyConnection(Object o) {
        return Mono.empty();
    }

    /**
     * 测试与 Barda API 的数据源的连接。
     *
     * @param connectionConfig 连接配置
     * @return 测试结果
     */
    @Override
    public Mono<DatasourceTestResult> testConnection(BardaApiDatasourceConfig connectionConfig) {
        return Mono.just(DatasourceTestResult.testSuccess());
    }

    /**
     * 解析数据源配置。
     *
     * @param configMap 配置 Map
     * @return 解析后的 BardaApiDatasourceConfig 对象
     */
    @Nonnull
    @Override
    public BardaApiDatasourceConfig resolveConfig(Map<String, Object> configMap) {
        return BardaApiDatasourceConfig.INSTANCE;
    }

    /**
     * 验证数据源配置。
     *
     * @param connectionConfig 连接配置
     * @return 验证结果的集合
     */
    @Override
    public Set<String> validateConfig(BardaApiDatasourceConfig connectionConfig) {
        return Collections.emptySet();
    }
}

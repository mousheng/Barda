package com.barda.domain.datasource.service.impl;

import static com.barda.infra.perf.PerfEvent.CLIENT_BASED_CONNECTION_CREATE;
import static com.barda.infra.perf.PerfEvent.CLIENT_BASED_CONNECTION_REMOVE;
import static com.barda.infra.perf.PerfEvent.CLIENT_BASED_CONNECTION_SIZE;
import static com.barda.infra.perf.PerfEvent.HIKARI_POOL_ACTIVE_CONNECTIONS;
import static com.barda.infra.perf.PerfEvent.HIKARI_POOL_IDLE_CONNECTIONS;
import static com.barda.infra.perf.PerfEvent.HIKARI_POOL_TOTAL_CONNECTIONS;
import static com.barda.infra.perf.PerfEvent.HIKARI_POOL_WAITING_CONNECTIONS;
import static com.barda.sdk.exception.BizError.PLUGIN_CREATE_CONNECTION_FAILED;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.querySharedScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableList;
import com.barda.domain.datasource.model.ClientBasedDatasourceConnectionHolder;
import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceConnectionHolder;
import com.barda.domain.datasource.service.DatasourceConnectionPool;
import com.barda.domain.plugin.DatasourceMetaInfo;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.infra.perf.PerfEvent;
import com.barda.infra.perf.PerfHelper;
import com.barda.sdk.exception.BaseException;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.plugin.common.QueryExecutionUtils;
import com.barda.sdk.plugin.common.sql.HikariPerfWrapper;

import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 该类管理各种数据源类型的连接，例如 Hikari Pool、Redis 客户端和 Elasticsearch 客户端。
 * 这些客户端接管基础连接并管理其状态。
 * 连接将被缓存和在数据源更新时进行无效化。
 */
@Slf4j
@Service
public class ClientBasedConnectionPool implements DatasourceConnectionPool {

    // 重试获取连接的次数
    private static final int DEFAULT_RETRIEVE_CONNECTION_TIMES = 5;

    // 用于 Hikari Pool 性能指标的事件列表
    private static final List<PerfEvent> HIKARI_PERF_CONFIG = ImmutableList.of(
            HIKARI_POOL_ACTIVE_CONNECTIONS,
            HIKARI_POOL_IDLE_CONNECTIONS,
            HIKARI_POOL_WAITING_CONNECTIONS,
            HIKARI_POOL_TOTAL_CONNECTIONS
    );

    // 用于存储 HikariPerfWrapper 对象的键值对
    private static final Map<ClientBasedDatasourceCacheKey, HikariPerfWrapper> HIKARI_PERF_WRAPPER_MAP = new ConcurrentHashMap<>();

    // 用于获取数据源元信息的服务
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    // 用于执行性能指标的帮助器
    @Autowired
    private PerfHelper perfHelper;

    // 初始化方法，在应用启动时执行
    @PostConstruct
    public void init() {
        // 获取支持的 Java 基础数据源类型
        List<DatasourceMetaInfo> supportedDatasourceTypes = datasourceMetaInfoService.getJavaBasedSupportedDatasourceMetaInfos();

        // 过滤出使用 ClientBasedConnectionPool 作为连接池的类型
        supportedDatasourceTypes.stream()
                .filter(datasourceMetaInfo -> datasourceMetaInfo.getConnectionPool() == ClientBasedConnectionPool.class)
                .forEach(datasourceMetaInfo ->
                        // 记录每个数据源类型的连接池大小
                        perfHelper.gaugeSafely(CLIENT_BASED_CONNECTION_SIZE, Tags.of("type", datasourceMetaInfo.getType()), cache,
                                value -> value.asMap().keySet()
                                        .stream()
                                        .filter(cacheKey -> cacheKey.datasource().getType().equals(datasourceMetaInfo.getType()))
                                        .toList()
                                        .size()));

        // 迭代所有数据源类型
        for (DatasourceMetaInfo metaInfo : supportedDatasourceTypes) {
            String datasourceType = metaInfo.getType();
            // 迭代 HIKARI_PERF_CONFIG 列表中的事件
            for (var perfEvent : HIKARI_PERF_CONFIG) {
                // 记录每个数据源类型的 Hikari Pool 性能指标
                perfHelper.gaugeSafely(perfEvent, Tags.of("datasourceType", datasourceType), HIKARI_PERF_WRAPPER_MAP,
                        perfWrapperMap -> perfWrapperMap.entrySet()
                                .stream()
                                .filter(entry -> StringUtils.equals(entry.getKey().datasource().getType(), datasourceType))
                                .map(Entry::getValue)
                                .mapToInt(hikariPerfWrapper -> switch (perfEvent) {
                                    case HIKARI_POOL_ACTIVE_CONNECTIONS -> hikariPerfWrapper.getActiveConnections();
                                    case HIKARI_POOL_IDLE_CONNECTIONS -> hikariPerfWrapper.getIdleConnections();
                                    case HIKARI_POOL_WAITING_CONNECTIONS -> hikariPerfWrapper.getWaitingConnections();
                                    case HIKARI_POOL_TOTAL_CONNECTIONS -> hikariPerfWrapper.getTotalConnections();
                                    default -> 0;
                                })
                                .sum());
            }
        }
    }

    // 用于缓存数据源连接的 LoadingCache
    private final LoadingCache<ClientBasedDatasourceCacheKey, Mono<ClientBasedDatasourceConnectionHolder>> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.ofHours(1L)) // 1 小时后过期
            .maximumSize(1000) // 最大缓存 1000 个条目
            .removalListener(new RemovalListener<ClientBasedDatasourceCacheKey, Mono<ClientBasedDatasourceConnectionHolder>>() {
                // 缓存项被移除时执行的操作
                @Override
                public void onRemoval(
                        @Nonnull RemovalNotification<ClientBasedDatasourceCacheKey, Mono<ClientBasedDatasourceConnectionHolder>> notification) {

                    ClientBasedDatasourceCacheKey key = notification.getKey();
                    String type = key.datasource().getType();
                    // 记录连接被移除的事件
                    perfHelper.count(CLIENT_BASED_CONNECTION_REMOVE, Tags.of("type", type, "cause", notification.getCause().name()));

                    // 从 HIKARI_PERF_WRAPPER_MAP 中移除键值对
                    HIKARI_PERF_WRAPPER_MAP.remove(key);

                    // 销毁数据源的连接
                    Mono.just(datasourceMetaInfoService.getDatasourceConnector(key.datasource().getType()))
                            .flatMap(factory -> notification.getValue().flatMap(connection -> factory.destroyConnection(connection.connection())))
                            .subscribeOn(querySharedScheduler())
                            .subscribe();
                }
            })
            .build(new CacheLoader<>() {
                // 加载缓存项时执行的操作
                @Override
                public Mono<ClientBasedDatasourceConnectionHolder> load(@Nonnull ClientBasedDatasourceCacheKey key) {
                    Datasource datasource = key.datasource();
                    // 记录创建连接的事件
                    perfHelper.count(CLIENT_BASED_CONNECTION_CREATE, Tags.of("type", datasource.getType()));

                    // 释放之前的数据源的连接
                    releasePreviousConnection(datasource);

                    // 创建新的数据源连接并返回
                    return create(datasource)
                            .doOnNext(connection -> {
                                // 如果连接是 HikariPerfWrapper 类型，则将其添加到 HIKARI_PERF_WRAPPER_MAP
                                if (connection.connection() instanceof HikariPerfWrapper wrapper) {
                                    HIKARI_PERF_WRAPPER_MAP.put(key, wrapper);
                                }
                            })
                            .cache();
                }
            });

    // 释放之前的数据源的连接
    private void releasePreviousConnection(Datasource datasource) {
        cache.asMap().keySet()
                .stream()
                .filter(cacheKey -> StringUtils.equals(datasource.getId(), cacheKey.datasource().getId()))
                .forEach(cache::invalidate);
    }

    // 获取或创建数据源的连接
    @Override
    public Mono<? extends DatasourceConnectionHolder> getOrCreateConnection(Datasource datasource) {
        ClientBasedDatasourceCacheKey clientBasedDatasourceCacheKey = ClientBasedDatasourceCacheKey.of(datasource);
        return Mono.defer(() -> cache.getUnchecked(clientBasedDatasourceCacheKey))
                .flatMap(clientBasedDatasourceConnection -> {
                    // 如果数据源连接已过期，则重试
                    if (clientBasedDatasourceConnection.isStale()) {
                        cache.invalidate(clientBasedDatasourceCacheKey);
                        return Mono.error(new RuntimeException("stale datasource")); // by retry
                    }
                    return Mono.just(clientBasedDatasourceConnection);
                })
                .retry(DEFAULT_RETRIEVE_CONNECTION_TIMES)
                .onErrorMap(throwable -> {
                    if (throwable instanceof BaseException) {
                        return throwable;
                    }
                    log.error("get connection error.", throwable);
                    return new BizException(PLUGIN_CREATE_CONNECTION_FAILED, "PLUGIN_CREATE_CONNECTION_FAILED", throwable.getMessage());
                })
                .subscribeOn(QueryExecutionUtils.querySharedScheduler());
    }

    // 获取数据源的性能指标
    @Override
    public Object info(@Nullable String datasourceId) {
        return HIKARI_PERF_WRAPPER_MAP.entrySet()
                .stream()
                .filter(entry -> {
                    if (StringUtils.isBlank(datasourceId)) {
                        return true;
                    }
                    return datasourceId.equals(entry.getKey().id());
                })
                .limit(100)
                .map(entry -> {
                    HikariPerfWrapper wrapper = entry.getValue();
                    Map<String, Integer> connections = Map.of("total", wrapper.getTotalConnections(),
                            "idle", wrapper.getIdleConnections(),
                            "active", wrapper.getActiveConnections(),
                            "waiting", wrapper.getWaitingConnections());
                    return Map.of("connections", connections,
                            "datasource", wrapper.getDatasourceProperties(),
                            "healthCheck", wrapper.getHealthCheckProperties());
                })
                .collect(Collectors.toList());
    }

    // 创建数据源的连接
    private Mono<ClientBasedDatasourceConnectionHolder> create(Datasource datasource) {
        return datasourceMetaInfoService.getDatasourceConnector(datasource.getType())
                .doCreateConnection(datasource.getDetailConfig())
                .map(ClientBasedDatasourceConnectionHolder::new);
    }

    /**
     * 用于 equals() 和 hashCode() 方法的数据源键
     */
    public record ClientBasedDatasourceCacheKey(String id, Instant updateTime, Datasource datasource) {

        public static ClientBasedDatasourceCacheKey of(Datasource datasource) {
            return new ClientBasedDatasourceCacheKey(datasource.getId(), datasource.getUpdatedAt(), datasource);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClientBasedDatasourceCacheKey that = (ClientBasedDatasourceCacheKey) o;
            return Objects.equals(id, that.id) && Objects.equals(updateTime, that.updateTime);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, updateTime);
        }
    }
}
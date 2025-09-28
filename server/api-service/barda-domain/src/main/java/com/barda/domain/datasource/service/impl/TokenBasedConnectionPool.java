package com.barda.domain.datasource.service.impl;

import static com.barda.domain.datasource.model.TokenBasedConnectionHolder.EMPTY_CONNECTION;
import static com.barda.sdk.exception.BizError.DATASOURCE_TYPE_ERROR;
import static com.barda.sdk.exception.BizError.PLUGIN_CREATE_CONNECTION_FAILED;
import static com.barda.sdk.util.ExceptionUtils.deferredError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.barda.domain.datasource.model.Datasource;
import com.barda.domain.datasource.model.DatasourceConnectionHolder;
import com.barda.domain.datasource.model.TokenBasedConnection;
import com.barda.domain.datasource.model.TokenBasedConnectionHolder;
import com.barda.domain.datasource.repository.TokenBasedConnectionRepository;
import com.barda.domain.datasource.service.DatasourceConnectionPool;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.models.TokenBasedConnectionDetail;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 实现基于令牌的数据源连接池的服务类。
 * 该类提供创建和获取基于令牌的数据源连接的功能。
 */
@Service
@Slf4j
public class TokenBasedConnectionPool implements DatasourceConnectionPool {

    /**
     * 用于获取数据源元信息的服务
     */
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    /**
     * 用于存储基于令牌的数据源连接的仓库
     */
    @Autowired
    private TokenBasedConnectionRepository connectionRepository;

    /**
     * 获取或创建数据源的基于令牌的连接。
     *
     * @param datasource 数据源
     * @return 包含基于令牌的数据源连接持有者的 Mono
     */
    @Override
    public Mono<? extends DatasourceConnectionHolder> getOrCreateConnection(Datasource datasource) {
        String datasourceId = datasource.getId();
        var connectionFactory = datasourceMetaInfoService.getDatasourceConnector(datasource.getType());

        return connectionRepository.findByDatasourceId(datasourceId, datasource.getType())
                .map(TokenBasedConnectionHolder::new)
                .defaultIfEmpty(EMPTY_CONNECTION)
                .flatMap(connection -> {
                    if (connection.isStale(datasource.getUpdatedAt())) {
                        return connectionFactory.doCreateConnection(datasource.getDetailConfig())
                                .switchIfEmpty(deferredError(PLUGIN_CREATE_CONNECTION_FAILED, "DATASOURCE_CONNECT_ERROR"))
                                .flatMap(newConnection -> saveNewConnection(datasource.getId(), newConnection));
                    }

                    return Mono.just(connection);
                });
    }

    /**
     * 获取数据源的相关信息。
     *
     * 请注意，此方法在基于令牌的数据源连接池中未实现。
     *
     * @param datasourceId 数据源 ID
     * @return 未实现的 UnsupportedOperationException
     */
    @Override
    public Object info(String datasourceId) {
        throw new UnsupportedOperationException();
    }

    /**
     * 保存新的基于令牌的数据源连接
     *
     * @param datasourceId 数据源 ID
     * @param connection 基于令牌的数据源连接
     * @return 包含基于令牌的数据源连接持有者的 Mono
     */
    private Mono<TokenBasedConnectionHolder> saveNewConnection(String datasourceId, Object connection) {

        if (!(connection instanceof TokenBasedConnectionDetail connectionDetail)) {
            throw new BizException(DATASOURCE_TYPE_ERROR, "DATASOURCE_CONNECTION_TYPE_ERROR", connection.getClass().getSimpleName());
        }

        TokenBasedConnection tokenBasedConnection = new TokenBasedConnection();
        tokenBasedConnection.setDatasourceId(datasourceId);
        tokenBasedConnection.setTokenDetail(connectionDetail);
        return connectionRepository.saveConnection(tokenBasedConnection, datasourceId)
                .thenReturn(new TokenBasedConnectionHolder(tokenBasedConnection));
    }

}


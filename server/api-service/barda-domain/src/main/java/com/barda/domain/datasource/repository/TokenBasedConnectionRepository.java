package com.barda.domain.datasource.repository;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.barda.domain.datasource.model.TokenBasedConnection;
import com.barda.domain.datasource.model.TokenBasedConnectionDO;
import com.barda.domain.encryption.EncryptionService;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.infra.mongo.MongoUpsertHelper;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.models.TokenBasedConnectionDetail;
import com.barda.sdk.plugin.common.DatasourceConnector;

import reactor.core.publisher.Mono;

/**
 * 基于令牌的数据库连接仓库。
 * 该类提供对持久化存储的TokenBasedConnectionDO的操作。
 */
@Repository
public class TokenBasedConnectionRepository {

    /**
     * 持久化存储的TokenBasedConnectionDO的操作接口。
     */
    @Autowired
    private TokenBasedConnectionDORepository tokenBasedConnectionDORepository;

    /**
     * 提供数据源元信息的服务。
     */
    @Autowired
    private DatasourceMetaInfoService datasourceMetaInfoService;

    /**
     * 用于对敏感信息进行加解密的服务。
     */
    @Autowired
    private EncryptionService encryptionService;

    /**
     * 用于执行MongoDB upsert操作的辅助类。
     */
    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    /**
     * 根据数据源ID和数据源类型查找TokenBasedConnection。
     *
     * @param datasourceId   数据源ID
     * @param datasourceType 数据源类型
     * @return 找到的TokenBasedConnection，如果没有找到则返回Mono.empty()
     */
    public Mono<TokenBasedConnection> findByDatasourceId(String datasourceId, String datasourceType) {
        return tokenBasedConnectionDORepository.findByDatasourceId(datasourceId)
                .map(connectionDO -> convertToDatasourceConnection(connectionDO, datasourceType));
    }

    /**
     * 保存TokenBasedConnection到持久化存储中。
     *
     * @param tokenBasedConnection 要保存的TokenBasedConnection
     * @param datasourceId         所属数据源ID
     * @return 保存操作完成的Mono
     */
    public Mono<Void> saveConnection(TokenBasedConnection tokenBasedConnection, String datasourceId) {

        tokenBasedConnection.getTokenDetail().doEncrypt(encryptionService::encryptString);

        TokenBasedConnectionDO result = new TokenBasedConnectionDO();
        result.setDatasourceId(tokenBasedConnection.getDatasourceId());
        result.setTokenDetail(tokenBasedConnection.getTokenDetail().toMap());
        result.setId(tokenBasedConnection.getId());
        result.setCreatedAt(tokenBasedConnection.getCreatedAt());
        result.setUpdatedAt(tokenBasedConnection.getUpdatedAt());
        result.setCreatedBy(tokenBasedConnection.getCreatedBy());
        result.setModifiedBy(tokenBasedConnection.getModifiedBy());
        return mongoUpsertHelper.upsertWithAuditingParams(result, "datasourceId", datasourceId)
                .doOnNext(__ -> tokenBasedConnection.getTokenDetail().doDecrypt(encryptionService::decryptString))
                .then();
    }

    /**
     * 将TokenBasedConnectionDO转换为TokenBasedConnection。
     *
     * @param tokenBasedConnectionDO 要转换的TokenBasedConnectionDO
     * @param datasourceType         数据源类型
     * @return 转换后的TokenBasedConnection
     */
    private TokenBasedConnection convertToDatasourceConnection(TokenBasedConnectionDO tokenBasedConnectionDO,
            String datasourceType) {

        Map<String, Object> tokenDetailMap = tokenBasedConnectionDO.getTokenDetail();
        TokenBasedConnectionDetail tokenDetail = getDatasourceConnector(datasourceType).resolveTokenDetail(tokenDetailMap);
        tokenDetail.doDecrypt(encryptionService::decryptString);

        TokenBasedConnection result = new TokenBasedConnection();
        result.setDatasourceId(tokenBasedConnectionDO.getDatasourceId());
        result.setTokenDetail(tokenDetail);
        result.setId(tokenBasedConnectionDO.getId());
        result.setCreatedAt(tokenBasedConnectionDO.getCreatedAt());
        result.setUpdatedAt(tokenBasedConnectionDO.getUpdatedAt());
        result.setCreatedBy(tokenBasedConnectionDO.getCreatedBy());
        result.setModifiedBy(tokenBasedConnectionDO.getModifiedBy());
        return result;
    }

    /**
     * 获取指定数据源类型的DatasourceConnector。
     *
     * @param datasourceType 数据源类型
     * @return 对应的DatasourceConnector
     */
    private DatasourceConnector<Object, ? extends DatasourceConnectionConfig> getDatasourceConnector(String datasourceType) {
        return datasourceMetaInfoService.getDatasourceConnector(datasourceType);
    }
}

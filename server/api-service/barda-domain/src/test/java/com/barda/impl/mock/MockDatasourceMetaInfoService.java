package com.barda.impl.mock;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

import com.barda.domain.plugin.DatasourceMetaInfo;
import com.barda.domain.plugin.service.DatasourceMetaInfoService;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.plugin.common.DatasourceConnector;
import com.barda.sdk.plugin.common.QueryExecutor;
import com.barda.sdk.query.QueryExecutionContext;

public class MockDatasourceMetaInfoService implements DatasourceMetaInfoService {

    MockDatasourceConnector mockDatasourceConnectionFactory = new MockDatasourceConnector();

    @Override
    public List<DatasourceMetaInfo> getJavaBasedSupportedDatasourceMetaInfos() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isJavaDatasourcePlugin(String type) {
        return false;
    }

    @Override
    public boolean isJsDatasourcePlugin(String type) {
        return false;
    }

    @Override
    public Flux<DatasourceMetaInfo> getAllSupportedDatasourceMetaInfos() {
        return null;
    }

    @Override
    public DatasourceMetaInfo getDatasourceMetaInfo(String datasourceType) {
        throw new UnsupportedOperationException();
    }


    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public DatasourceConnector<Object, ? extends DatasourceConnectionConfig> getDatasourceConnector(String datasourceMetaInfo) {
        return (DatasourceConnector) mockDatasourceConnectionFactory;
    }

    @Override
    public QueryExecutor<? extends DatasourceConnectionConfig, Object, ? extends QueryExecutionContext> getQueryExecutor(String datasourceMetaInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DatasourceConnectionConfig resolveDetailConfig(Map<String, Object> datasourceDetailMap, String datasourceMetaInfo) {
        throw new UnsupportedOperationException();
    }

}

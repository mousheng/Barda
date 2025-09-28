package com.barda.impl.mock;

import com.barda.domain.datasource.model.Datasource;
import com.barda.sdk.models.DatasourceConnectionConfig;

public record MockDatasourceConnectionConfig(Datasource datasource) implements DatasourceConnectionConfig {

    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig detailConfig) {
        throw new UnsupportedOperationException();
    }
}

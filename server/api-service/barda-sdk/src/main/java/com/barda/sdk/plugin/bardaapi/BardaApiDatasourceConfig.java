package com.barda.sdk.plugin.bardaapi;

import com.barda.sdk.models.DatasourceConnectionConfig;

/**
 * BardaApiDatasourceConfig实现了DatasourceConnectionConfig接口，用于配置Barda API数据源连接。
 */
public class BardaApiDatasourceConfig implements DatasourceConnectionConfig {

    /**
     * BardaApiDatasourceConfig的单例对象。
     */
    public static final BardaApiDatasourceConfig INSTANCE = new BardaApiDatasourceConfig();

    /**
     * 将传入的详细配置与当前配置进行合并。
     *
     * @param detailConfig 要合并的详细配置
     * @return 合并后的配置
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig detailConfig) {
        return detailConfig;
    }
}


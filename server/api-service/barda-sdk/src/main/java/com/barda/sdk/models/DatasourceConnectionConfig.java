package com.barda.sdk.models;

import java.util.function.Function;

/**
 * 数据源连接配置接口。
 * 定义了数据源连接的通用配置。
 */
public interface DatasourceConnectionConfig {

    /**
     * 合并指定的详细配置，并返回一个新的 {@link DatasourceConnectionConfig} 对象。
     *
     * @param detailConfig 详细的配置
     * @return 合并后的新配置
     */
    DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig detailConfig);

    /**
     * 子类可以重写此方法来实现加密功能。
     *
     * @param encryptFunc 加密函数
     * @return 加密后的配置
     */
    default DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        return this;
    }

    /**
     * 子类可以重写此方法来实现解密功能。
     *
     * @param decryptFunc 解密函数
     * @return 解密后的配置
     */
    default DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        return this;
    }
}

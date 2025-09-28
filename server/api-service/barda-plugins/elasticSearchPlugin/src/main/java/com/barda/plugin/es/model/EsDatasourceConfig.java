package com.barda.plugin.es.model;

import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_CONFIG_TYPE;
import static com.barda.sdk.util.ExceptionUtils.ofException;

import java.util.function.Function;

import org.apache.commons.lang3.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonView;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.models.DatasourceConnectionConfig;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Elasticsearch 数据源配置类。
 * 实现了 {@link DatasourceConnectionConfig} 接口，提供数据源连接配置的相关功能。
 */
@Slf4j
@Setter
@Getter
@Builder
public class EsDatasourceConfig implements DatasourceConnectionConfig {

    /**
     * Elasticsearch 连接字符串。
     */
    private String connectionString;

    /**
     * Elasticsearch 连接用户名。
     */
    private String username;

    /**
     * Elasticsearch 连接密码。
     * 标记为 {@link JsonView}，在内部使用时可见。
     */
    @JsonView(JsonViews.Internal.class)
    private String password;

    /**
     * 是否使用 SSL 连接。
     */
    private Boolean usingSsl;

    /**
     * 合并更新的配置并返回新的 {@link EsDatasourceConfig} 实例。
     *
     * @param updatedConfig 要合并的更新的配置
     * @return 合并后的新 {@link EsDatasourceConfig} 实例
     * @throws IllegalArgumentException 如果 {@code updatedConfig} 不是 {@link EsDatasourceConfig} 类型
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig updatedConfig) {
        if (!(updatedConfig instanceof EsDatasourceConfig esDatasourceConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE", updatedConfig.getClass().getSimpleName());
        }

        return EsDatasourceConfig.builder()
                .connectionString(esDatasourceConfig.getConnectionString())
                .username(esDatasourceConfig.getUsername())
                .password(ObjectUtils.firstNonNull(esDatasourceConfig.getPassword(), getPassword()))
                .usingSsl(esDatasourceConfig.getUsingSsl())
                .build();
    }

    /**
     * 对密码进行加密。
     *
     * @param encryptFunc 用于加密的函数
     * @return 已加密的 {@link EsDatasourceConfig} 实例
     */
    @Override
    public DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        try {
            password = encryptFunc.apply(password);
            return this;
        } catch (Exception e) {
            log.error("fail to encrypt password: {}", password, e);
            return this;
        }
    }

    /**
     * 对密码进行解密。
     *
     * @param decryptFunc 用于解密的函数
     * @return 已解密的 {@link EsDatasourceConfig} 实例
     */
    @Override
    public DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        try {
            password = decryptFunc.apply(password);
            return this;
        } catch (Exception e) {
            log.error("fail to encrypt password: {}", password, e);
            return this;
        }
    }
}

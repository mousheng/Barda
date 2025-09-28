package com.barda.plugins;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.function.Function;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonView;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.models.DatasourceConnectionConfig;

import lombok.Builder;
import lombok.Getter;

/**
 * 用于存储和表示SMTP数据源配置的类。
 * 该类实现了DatasourceConnectionConfig接口，并使用了Lombok的@Getter、@Builder注解来生成getter方法和构建器。
 */
@Getter
@Builder
public class SmtpDatasourceConfig implements DatasourceConnectionConfig {

    private final String host;
    private final int port;

    @Nullable
    private final String username;
    @Nullable
    @JsonView(JsonViews.Internal.class)
    private String password;

    /**
     * 合并更新的数据源配置。
     *
     * @param detailConfig 要更新的数据源配置
     * @return 合并后的新的数据源配置
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig detailConfig) {
        SmtpDatasourceConfig updateConfig = (SmtpDatasourceConfig) detailConfig;
        return SmtpDatasourceConfig.builder()
                .host(updateConfig.getHost())
                .port(updateConfig.getPort())
                .username(updateConfig.getUsername())
                .password(firstNonNull(updateConfig.getPassword(), this.getPassword()))
                .build();
    }

    /**
     * 对数据源配置进行加密。
     *
     * @param encryptFunc 用于加密的函数
     * @return 已加密的数据源配置
     */
    @Override
    public DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        if (isNotBlank(this.password)) {
            this.password = encryptFunc.apply(this.password);
        }
        return this;
    }

    /**
     * 对数据源配置进行解密。
     *
     * @param decryptFunc 用于解密的函数
     * @return 已解密的数据源配置
     */
    @Override
    public DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        if (isNotBlank(this.password)) {
            this.password = decryptFunc.apply(this.password);
        }
        return this;
    }
}

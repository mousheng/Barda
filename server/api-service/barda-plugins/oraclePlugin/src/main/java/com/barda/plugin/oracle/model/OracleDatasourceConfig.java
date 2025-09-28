package com.barda.plugin.oracle.model;

import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_CONFIG_TYPE;
import static com.barda.sdk.util.ExceptionUtils.ofException;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.barda.sdk.models.DatasourceConnectionConfig;
import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;

import lombok.experimental.SuperBuilder;

/**
 * OracleDatasourceConfig 类是 SqlBasedDatasourceConnectionConfig 的子类，
 * 用于表示 Oracle 数据库的数据源配置。
 * 它使用了 Lombok 的 @SuperBuilder 注解来生成构造器。
 */
@SuperBuilder
public class OracleDatasourceConfig extends SqlBasedDatasourceConnectionConfig {
    private final String sid;
    private final String serviceName;
    private final String jdbcUrl;

    /**
     * 私有构造函数，仅在本类中使用。
     *
     * @param username 用户名
     * @param password 密码
     * @param host 主机
     * @param port 端口
     * @param sid SID
     * @param serviceName 服务名称
     * @param jdbcUrl JDBC URL
     * @param enableTurnOffPreparedStatement 是否启用关闭 PreparedStatement
     * @param isReadonly 是否只读
     * @param extParams 扩展参数
     */
    @JsonCreator
    private OracleDatasourceConfig(String username, String password, String host,
            Long port, String sid, String serviceName, String jdbcUrl,
            boolean enableTurnOffPreparedStatement, boolean isReadonly, Map<String, Object> extParams) {
        super("", username, password, host, port, false, "", isReadonly, enableTurnOffPreparedStatement, extParams);
        this.sid = sid;
        this.serviceName = serviceName;
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * 重写 mergeWithUpdatedConfig 方法，
     * 用于将新配置与现有配置合并。
     *
     * @param detailConfig 新配置
     * @return 合并后的配置
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig detailConfig) {
        if (!(detailConfig instanceof OracleDatasourceConfig newConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE",
                    detailConfig.getClass().getSimpleName());
        }

        return OracleDatasourceConfig.builder()
                .username(newConfig.getUsername())
                .password(ObjectUtils.firstNonNull(newConfig.getPassword(), getPassword()))
                .host(newConfig.getHost())
                .port(newConfig.getPort())
                .jdbcUrl(newConfig.getJdbcUrl())
                .sid(newConfig.getSid())
                .serviceName(newConfig.getServiceName())
                .enableTurnOffPreparedStatement(newConfig.isEnableTurnOffPreparedStatement())
                .isReadonly(newConfig.isReadonly())
                .extParams(newConfig.getExtParams())
                .build();
    }

    /**
     * 获取 SID。
     *
     * @return SID
     */
    public String getSid() {
        return sid;
    }

    /**
     * 获取服务名称。
     *
     * @return 服务名称
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 获取 JDBC URL。
     * 如果已指定 JDBC URL，则返回该 URL。
     * 否则，根据 SID 或服务名称生成 JDBC URL。
     *
     * @return JDBC URL
     */
    public String getJdbcUrl() {
        if (StringUtils.isNotBlank(jdbcUrl)) {
            return jdbcUrl;
        }
        if (StringUtils.isNotBlank(sid)) {
            return String.format("jdbc:oracle:thin:@%s:%s:%s", getHost(), getPort(), getSid());
        }
        return String.format("jdbc:oracle:thin:@//%s:%s/%s", getHost(), getPort(), getServiceName());
    }

    /**
     * 重写 defaultPort 方法，
     * 用于返回 Oracle 数据库的默认端口。
     *
     * @return 默认端口
     */
    @Override
    protected long defaultPort() {
        return 1521;
    }
}

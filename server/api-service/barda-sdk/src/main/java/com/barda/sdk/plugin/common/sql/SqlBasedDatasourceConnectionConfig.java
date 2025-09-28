package com.barda.sdk.plugin.common.sql;

import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_CONFIG_TYPE;
import static com.barda.sdk.util.ExceptionUtils.ofException;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonView;
import com.barda.sdk.config.SerializeConfig.JsonViews;
import com.barda.sdk.exception.ServerException;
import com.barda.sdk.models.DatasourceConnectionConfig;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * SqlBasedDatasourceConnectionConfig是一个抽象类，实现了DatasourceConnectionConfig接口，用于基于SQL的数据源连接配置。
 */
@Slf4j
@Getter
@SuperBuilder
public abstract class SqlBasedDatasourceConnectionConfig implements DatasourceConnectionConfig {

    /**
     * 数据库名称
     */
    private final String database;

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码
     */
    @JsonView(JsonViews.Internal.class)
    private String password;

    /**
     * 主机
     */
    private final String host;

    /**
     * 端口
     */
    private final Long port;

    /**
     * 是否使用SSL
     */
    private final boolean usingSsl;

    /**
     * 服务器时区
     */
    private final String serverTimezone;

    /**
     * 是否只读
     */
    private final boolean isReadonly;

    /**
     * 是否启用关闭PreparedStatement
     */
    private final boolean enableTurnOffPreparedStatement;

    /**
     * 扩展参数
     */
    private final Map<String, Object> extParams;

    /**
     * 构造方法，用于创建SqlBasedDatasourceConnectionConfig实例。
     *
     * @param database                     数据库名称
     * @param username                     用户名
     * @param password                     密码
     * @param host                         主机
     * @param port                         端口
     * @param usingSsl                     是否使用SSL
     * @param serverTimezone               服务器时区
     * @param isReadonly                   是否只读
     * @param enableTurnOffPreparedStatement 是否启用关闭PreparedStatement
     * @param extParams                    扩展参数
     */
    protected SqlBasedDatasourceConnectionConfig(String database, String username, String password, String host, Long port,
            boolean usingSsl, String serverTimezone, boolean isReadonly,
            boolean enableTurnOffPreparedStatement,
            Map<String, Object> extParams) {
        this.database = database;
        this.username = username;
        this.password = password;
        this.usingSsl = usingSsl;
        this.host = host;
        this.port = port;
        this.serverTimezone = serverTimezone;
        this.isReadonly = isReadonly;
        this.enableTurnOffPreparedStatement = enableTurnOffPreparedStatement;
        this.extParams = extParams;
    }

    /**
     * 抽象方法，用于返回默认端口。
     *
     * @return 默认端口值
     */
    protected abstract long defaultPort();

    /**
     * 获取数据库名称。
     *
     * @return 数据库名称
     */
    public final String getDatabase() {
        return StringUtils.trimToEmpty(database);
    }

    /**
     * 获取端口号。
     *
     * @return 端口号
     */
    public final long getPort() {
        return port == null ? defaultPort() : port;
    }

    /**
     * 获取密码。
     *
     * @return 密码
     */
    public final String getPassword() {
        return password;
    }

    /**
     * 是否使用SSL。
     *
     * @return true表示使用SSL，否则false
     */
    public final boolean isUsingSsl() {
        return usingSsl;
    }

    /**
     * 获取服务器时区。
     *
     * @return 服务器时区
     */
    public final String getServerTimezone() {
        return serverTimezone;
    }

    /**
     * 获取主机。
     *
     * @return 主机
     */
    public final String getHost() {
        return StringUtils.trimToEmpty(host);
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public final String getUsername() {
        return StringUtils.trimToEmpty(username);
    }

    /**
     * 是否只读。
     *
     * @return true表示只读，否则false
     */
    public final boolean isReadonly() {
        return isReadonly;
    }

    /**
     * 是否启用关闭PreparedStatement。
     *
     * @return true表示启用，否则false
     */
    public final boolean isEnableTurnOffPreparedStatement() {
        return enableTurnOffPreparedStatement;
    }

    /**
     * 获取扩展参数。
     *
     * @return 扩展参数
     */
    public Map<String, Object> getExtParams() {
        return emptyIfNull(extParams);
    }

    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig updatedDatasourceConnectionConfig) {
        if (!(updatedDatasourceConnectionConfig instanceof SqlBasedDatasourceConnectionConfig updatedConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE",
                    updatedDatasourceConnectionConfig.getClass().getSimpleName());
        }

        return createMergedConnectionConfig(
                updatedConfig.getDatabase(),
                updatedConfig.getUsername(),
                firstNonNull(updatedConfig.getPassword(), getPassword()), // use original password if new password is not provided
                updatedConfig.getHost(),
                updatedConfig.getPort(),
                updatedConfig.isUsingSsl(),
                updatedConfig.getServerTimezone(),
                updatedConfig.isReadonly(),
                updatedConfig.isEnableTurnOffPreparedStatement(),
                updatedConfig.getExtParams()
        );
    }

    /**
     * 创建合并后的连接配置。
     *
     * @param database                     数据库名称
     * @param username                     用户名
     * @param password                     密码
     * @param host                         主机
     * @param port                         端口
     * @param usingSsl                     是否使用SSL
     * @param serverTimezone               服务器时区
     * @param readonly                     是否只读
     * @param enableTurnOffPreparedStatement 是否启用关闭PreparedStatement
     * @param extParams                    扩展参数
     * @return 合并后的连接配置
     */
    private DatasourceConnectionConfig createMergedConnectionConfig(String database, String username, String password, String host,
            long port, boolean usingSsl, String serverTimezone, boolean readonly, boolean enableTurnOffPreparedStatement,
            Map<String, Object> extParams) {
        Constructor<?>[] constructors = getClass().getConstructors();
        try {
            return (DatasourceConnectionConfig) constructors[0].newInstance(database, username, password,
                    host, port, usingSsl, serverTimezone, readonly, enableTurnOffPreparedStatement, extParams);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ServerException("fail to create SQL data source: {0}", e.getMessage());
        }
    }

    @Override
    public final DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        try {
            password = encryptFunc.apply(password);
            return this;
        } catch (Exception e) {
            log.error("fail to encrypt password: {}", password, e);
            return this;
        }
    }

    @Override
    public final DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        try {
            password = decryptFunc.apply(password);
            return this;
        } catch (Exception e) {
            log.error("fail to encrypt password: {}", password, e);
            return this;
        }
    }
}

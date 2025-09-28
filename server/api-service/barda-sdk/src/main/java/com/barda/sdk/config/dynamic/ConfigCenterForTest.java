package com.barda.sdk.config.dynamic;

import java.util.Collections;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;

/**
 * 该类是一个用于测试的配置中心实现类，它实现了 ConfigCenter 接口。
 *
 * 该类使用了 @VisibleForTesting 注解来表示它是为测试而创建的。
 *
 * 该类在测试时可以用来覆盖一些配置的值。
 */
@VisibleForTesting
public class ConfigCenterForTest implements ConfigCenter {

    /**
     * 用来覆盖一些配置的值的键值对。
     *
     * 该字段使用了 Collections.emptyMap() 来初始化，表示它是空的。
     */
    private Map<String, Object> overrideKeyValues = Collections.emptyMap();

    /**
     * 一个有参的构造函数，用来在测试时覆盖一些配置的值。
     *
     * @param overrideKeyValues 用来覆盖一些配置的值的键值对
     */
    public ConfigCenterForTest(Map<String, Object> overrideKeyValues) {
        this.overrideKeyValues = overrideKeyValues;
    }

    /**
     * 一个无参的构造函数，用来在测试时使用默认的配置值。
     */
    public ConfigCenterForTest() {
    }

    @Override
    public ConfigInstance asset() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance mysqlPlugin() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance clickHousePlugin() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance mongoPlugin() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance postgresPlugin() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance oraclePlugin() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance threshold() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance proxy() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance auth() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance datasource() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance deployment() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }

    @Override
    public ConfigInstance application() {
        return new StaticConfigInstanceImpl(overrideKeyValues);
    }
}

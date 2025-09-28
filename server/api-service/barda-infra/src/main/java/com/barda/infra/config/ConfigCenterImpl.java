package com.barda.infra.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.sdk.config.dynamic.Conf;
import com.barda.sdk.config.dynamic.ConfigCenter;
import com.barda.sdk.config.dynamic.ConfigInstance;

/**
 * ConfigCenterImpl类实现了ConfigCenter接口，用于获取不同业务场景下的ConfigInstance。
 */
@Component
public class ConfigCenterImpl implements ConfigCenter {

    /**
     * delegateConfigInstance是委托的ConfigInstance对象，用于实际获取配置信息。
     */
    @Autowired
    private ConfigInstance delegateConfigInstance;

    /**
     * instanceMap是一个线程安全的ConcurrentHashMap，用于缓存不同业务场景下的ForwardingConfigInstance对象。
     */
    private final ConcurrentHashMap<String, ForwardingConfigInstance> instanceMap = new ConcurrentHashMap<>();

    /**
     * getInstance方法根据业务键获取对应的ConfigInstance对象。
     * 如果instanceMap中不存在该业务键对应的对象，则创建一个新的ForwardingConfigInstance对象并返回。
     *
     * @param bizKey 业务键
     * @return ConfigInstance对象
     */
    private ConfigInstance getInstance(String bizKey) {
        return instanceMap.computeIfAbsent(bizKey, s -> new ForwardingConfigInstance(s, delegateConfigInstance));
    }

    /**
     * asset方法获取资产相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance asset() {
        return getInstance("asset");
    }

    /**
     * mysqlPlugin方法获取MySQL插件相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance mysqlPlugin() {
        return getInstance("mysqlPlugin");
    }

    /**
     * clickHousePlugin方法获取ClickHouse插件相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance clickHousePlugin() {
        return new ForwardingConfigInstance("clickHousePlugin", delegateConfigInstance);
    }

    /**
     * mongoPlugin方法获取MongoDB插件相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance mongoPlugin() {
        return getInstance("mongoPlugin");
    }

    /**
     * postgresPlugin方法获取PostgreSQL插件相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance postgresPlugin() {
        return getInstance("postgresPlugin");
    }

    /**
     * oraclePlugin方法获取Oracle插件相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance oraclePlugin() {
        return getInstance("oraclePlugin");
    }

    /**
     * threshold方法获取阈值相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance threshold() {
        return getInstance("threshold");
    }

    /**
     * proxy方法获取代理相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance proxy() {
        return getInstance("proxy");
    }

    /**
     * auth方法获取认证相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance auth() {
        return getInstance("auth");
    }

    /**
     * datasource方法获取数据源相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance datasource() {
        return getInstance("datasource");
    }

    /**
     * deployment方法获取部署相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance deployment() {
        return getInstance("deployment");
    }

    /**
     * application方法获取应用相关的ConfigInstance对象。
     *
     * @return ConfigInstance对象
     */
    @Override
    public ConfigInstance application() {
        return getInstance("application");
    }

    /**
     * ForwardingConfigInstance是一个内部类，实现了ConfigInstance接口。
     * 它包含一个业务键和一个委托的ConfigInstance对象。
     * 通过调用委托的ConfigInstance对象的相应方法，实现ConfigInstance接口的方法。
     */
    private record ForwardingConfigInstance(String bizKey, ConfigInstance delegateConfigInstance) implements ConfigInstance {

        /**
         * ofInteger方法获取整数类型的配置信息。
         *
         * @param confKey      配置键
         * @param defaultValue 默认值
         * @return Conf<Integer>对象
         */
        @Override
        public Conf<Integer> ofInteger(String confKey, int defaultValue) {
            return delegateConfigInstance.ofInteger(bizKey + "." + confKey, defaultValue);
        }

        /**
         * ofString方法获取字符串类型的配置信息。
         *
         * @param confKey      配置键
         * @param defaultValue 默认值
         * @return Conf<String>对象
         */
        @Override
        public Conf<String> ofString(String confKey, String defaultValue) {
            return delegateConfigInstance.ofString(bizKey + "." + confKey, defaultValue);
        }

        /**
         * ofBoolean方法获取布尔类型的配置信息。
         *
         * @param confKey      配置键
         * @param defaultValue 默认值
         * @return Conf<Boolean>对象
         */
        @Override
        public Conf<Boolean> ofBoolean(String confKey, boolean defaultValue) {
            return delegateConfigInstance.ofBoolean(bizKey + "." + confKey, defaultValue);
        }

        /**
         * ofJson方法获取JSON类型的配置信息。
         *
         * @param confKey      配置键
         * @param tClass       目标类型
         * @param defaultValue 默认值
         * @return Conf<T>对象，其中T为目标类型
         */
        @Override
        public <T> Conf<T> ofJson(String confKey, Class<T> tClass, T defaultValue) {
            return delegateConfigInstance.ofJson(bizKey + "." + confKey, tClass, defaultValue);
        }

        /**
         * ofList方法获取列表类型的配置信息。
         *
         * @param confKey      配置键
         * @param defaultValue 默认值
         * @param tClass       列表元素类型
         * @return Conf<List<T>>对象，其中T为列表元素类型
         */
        @Override
        public <T> Conf<List<T>> ofList(String confKey, List<T> defaultValue, Class<T> tClass) {
            return delegateConfigInstance.ofList(bizKey + "." + confKey, defaultValue, tClass);
        }

        /**
         * ofStringList方法获取字符串列表类型的配置信息。
         *
         * @param confKey      配置键
         * @param defaultValue 默认值
         * @return Conf<List<String>>对象
         */
        @Override
        public Conf<List<String>> ofStringList(String confKey, List<String> defaultValue) {
            return delegateConfigInstance.ofStringList(bizKey + "." + confKey, defaultValue);
        }

        /**
         * ofIntList方法获取整数列表类型的配置信息。
         *
         * @param confKey      配置键
         * @param defaultValue 默认值
         * @return Conf<List<Integer>>对象
         */
        @Override
        public Conf<List<Integer>> ofIntList(String confKey, List<Integer> defaultValue) {
            return delegateConfigInstance.ofIntList(bizKey + "." + confKey, defaultValue);
        }

        /**
         * ofLongList方法获取长整数列表类型的配置信息。
         *
         * @param confKey      配置键
         * @param defaultValue 默认值
         * @return Conf<List<Long>>对象
         */
        @Override
        public Conf<List<Long>> ofLongList(String confKey, List<Long> defaultValue) {
            return delegateConfigInstance.ofLongList(bizKey + "." + confKey, defaultValue);
        }

        /**
         * 该方法用于创建一个具有默认值的Map类型的配置对象。
         *
         * @param confKey       配置键，用于在配置文件中查找对应的配置项
         * @param kClass        Map中键的类型
         * @param vClass        Map中值的类型
         * @param defaultValue  当配置文件中没有找到对应的配置项时，使用的默认值
         * @return              返回一个包含默认值的Map类型的配置对象
         */
        @Override
        public <K, V> Conf<Map<K, V>> ofMap(String confKey, Class<K> kClass, Class<V> vClass, Map<K, V> defaultValue) {
            return delegateConfigInstance.ofMap(bizKey + "." + confKey, kClass, vClass, defaultValue);
        }
    }
}

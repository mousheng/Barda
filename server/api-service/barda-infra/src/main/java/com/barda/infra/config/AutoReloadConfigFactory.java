package com.barda.infra.config;

import static com.barda.sdk.util.JsonUtils.toJson;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.time.Duration;
import java.util.Map;

import javax.annotation.Nullable;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.barda.infra.config.model.ServerConfig;
import com.barda.infra.config.repository.ServerConfigRepository;
import com.barda.infra.localcache.ReloadableCache;

import lombok.extern.slf4j.Slf4j;

/**
 * 自动重新加载配置工厂类，使用 Spring 的 @Component 注解来标记为 Spring 组件。
 *
 * 该类使用 Lombok 的 @Slf4j 注解来提供日志功能。
 *
 */
@Slf4j
@Component
class AutoReloadConfigFactory {

    /**
     * 服务器配置仓库接口的实例，用于从数据库中查询配置。
     *
     * 该类使用 Spring 的 @Autowired 注解来注入 ServerConfigRepository 实例。
     */
    @Autowired
    private ServerConfigRepository configRepository;

    /**
     * 用于自动重新加载配置的可重载缓存。
     *
     * 该缓存使用 ReloadableCache 类来实现，并使用 ServerConfigRepository 的 findAll() 方法来查询所有配置。
     * 然后，使用 Stream API 过滤出非空值并将其转换为不可修改的 Map。
     * 最后，使用 ReloadableCache 的 Builder 模式来配置缓存的名称、刷新间隔和数据工厂。
     */
    private ReloadableCache<Map<String, Object>> allConfigs;

    /**
     * 该方法在类初始化时被调用，用于初始化 allConfigs 缓存。
     *
     * 该方法使用 @PostConstruct 注解来标记为在类初始化后执行。
     */
    @PostConstruct
    private void init() {
        allConfigs = ReloadableCache.<Map<String, Object>> newBuilder()
                .setFactory(() -> configRepository.findAll()
                        .filter(it -> it.getValue() != null)
                        .collectList()
                        .map(configs -> configs.stream().collect(toUnmodifiableMap(ServerConfig::getKey, ServerConfig::getValue))))
                .setInterval(Duration.ofSeconds(3))
                .setName("autoReloadConfCache")
                .build();
    }

    /**
     * 获取指定配置项的值。
     *
     * 该方法从 allConfigs 缓存中获取指定键的值，并将其转换为 JSON 字符串返回。
     * 如果在缓存中找不到指定键的值，则返回 null。
     *
     * @param confKey 配置项的键
     * @return 配置项的值的 JSON 字符串，如果找不到则返回 null
     */
    @Nullable
    public String getValue(String confKey) {
        Object result = allConfigs.getCachedOrDefault(emptyMap()).get(confKey);
        if (result == null) {
            return null;
        }

        return toJson(result);
    }
}

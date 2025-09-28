package com.barda.sdk.models;

import static com.barda.sdk.exception.BizError.INVALID_DATASOURCE_CONFIG_TYPE;
import static com.barda.sdk.util.ExceptionUtils.ofException;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.annotation.Transient;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * 基于JS的数据源连接配置类。
 * 继承自HashMap并实现了DatasourceConnectionConfig接口。
 * 该类用于存储和操作数据源的连接配置。
 */
@Slf4j
@Getter
@Setter
public class JsDatasourceConnectionConfig extends HashMap<String, Object> implements DatasourceConnectionConfig {

    /**
     * 非持久化的定义对象，用于在运行时获取数据源的定义。
     */
    @Transient
    private Object definition;

    /**
     * 非持久化的数据源类型。
     */
    @Transient
    private String type;

    /**
     * 获取extra字段的值。
     *
     * @return extra字段的值
     */
    public Object getExtra() {
        return this.get("extra");
    }

    /**
     * 获取定义在插件中的静态密码类型参数的键集合。
     *
     * @return 静态密码类型参数的键集合
     */
    @SuppressWarnings({"unchecked"})
    private Set<String> getAllStaticPasswordTypeKeys() {
        Set<String> passwordTypeKeys = new HashSet<>();
        for (Object param : getParamsFromPluginDefinition()) {
            if (param instanceof Map map && "password".equals(map.get("type"))) {
                passwordTypeKeys.add(MapUtils.getString(map, "key"));
            }
        }
        return passwordTypeKeys;
    }

    /**
     * 获取定义在插件中的所有静态参数的键集合。
     *
     * @return 静态参数的键集合
     */
    @SuppressWarnings({"unchecked"})
    private Set<String> getAllStaticKeys() {
        Set<String> keys = new HashSet<>();
        for (Object param : getParamsFromPluginDefinition()) {
            if (param instanceof Map map) {
                keys.add(MapUtils.getString(map, "key"));
            }
        }
        return keys;
    }

    /**
     * 获取定义在插件中的所有动态参数的键集合。
     *
     * @return 动态参数的键集合
     */
    @SuppressWarnings("unchecked")
    private Set<String> getAllDynamicKeys() {
        Set<String> allKeys = new HashSet<>();
        if (this.get("dynamicParamsDef") instanceof List<?> extraParamsDefinitions) {
            for (Object extraParamsDefinition : extraParamsDefinitions) {
                if (extraParamsDefinition instanceof Map map) {
                    allKeys.add(MapUtils.getString(map, "key"));
                }
            }
        }
        return allKeys;
    }

    /**
     * 获取定义在插件中的所有动态密码类型参数的键集合。
     *
     * @return 动态密码类型参数的键集合
     */
    @SuppressWarnings("unchecked")
    private Set<String> getAllDynamicPasswordTypeKeys() {
        Set<String> allKeys = new HashSet<>();
        if (this.get("dynamicParamsDef") instanceof List<?> extraParamsDefinitions) {
            for (Object extraParamsDefinition : extraParamsDefinitions) {
                if (extraParamsDefinition instanceof Map map && "password".equals(map.get("type"))) {
                    allKeys.add(MapUtils.getString(map, "key"));
                }
            }
        }
        return allKeys;
    }

    /**
     * 获取动态参数的值。
     *
     * @param key 键
     * @return 键对应的值
     */
    @SuppressWarnings("unchecked")
    private Object getDynamicParamsValue(String key) {
        return Optional.ofNullable(MapUtils.getMap(this, "dynamicParamsConfig"))
                .map(map -> MapUtils.getObject((Map<String, Object>) map, key))
                .orElse(null);
    }

    /**
     * 获取插件定义中的参数列表。
     *
     * @return 参数列表
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<?> getParamsFromPluginDefinition() {
        if (definition == null) {
            log.error("definition is null: {}", type);
            return Collections.emptyList();
        }
        Map<Object, Object> dataSourceConfig = MapUtils.getMap((Map) definition, "dataSourceConfig", new HashMap<>());
        Object paramObject = dataSourceConfig.get("params");
        if (paramObject instanceof List<?> params) {
            return params;
        }
        return Collections.emptyList();
    }

    /**
     * 移除所有静态和动态的密码类型参数。
     */
    public void removePasswords() {
        for (String passwordKey : getAllStaticPasswordTypeKeys()) {
            this.remove(passwordKey);
        }
        for (String passwordKey : getAllDynamicPasswordTypeKeys()) {
            if (this.get("dynamicParamsConfig") instanceof Map<?, ?> map) {
                map.remove(passwordKey);
            }
        }
    }

    /**
     * 与另一个数据源连接配置进行合并，并返回一个新的JsDatasourceConnectionConfig实例。
     *
     * @param detailConfig 另一个数据源连接配置
     * @return 合并后的新JsDatasourceConnectionConfig实例
     */
    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig detailConfig) {
        if (!(detailConfig instanceof JsDatasourceConnectionConfig jsDatasourceConnectionConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE", detailConfig.getClass().getSimpleName());
        }
        if (!Objects.equals(this.type, jsDatasourceConnectionConfig.type)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE", jsDatasourceConnectionConfig.type);
        }
        JsDatasourceConnectionConfig newJsDatasourceConnectionConfig = new JsDatasourceConnectionConfig();

        // 静态参数
        Set<String> allKeys = getAllStaticKeys();
        Set<String> passwordTypeKeys = getAllStaticPasswordTypeKeys();
        for (String key : allKeys) {
            if (passwordTypeKeys.contains(key)) {
                // 如果是密码类型，使用旧值。
                newJsDatasourceConnectionConfig.put(key, firstNonNull(jsDatasourceConnectionConfig.get(key), this.get(key)));
                continue;
            }
            newJsDatasourceConnectionConfig.put(key, jsDatasourceConnectionConfig.get(key));
        }

        // 动态参数
        Set<String> allDynamicKeys = getAllDynamicKeys();
        Set<String> allDynamicPasswordTypeKeys = getAllDynamicPasswordTypeKeys();
        Map<String, Object> newDynamicParamsConfig = new HashMap<>();
        for (String key : allDynamicKeys) {
            if (allDynamicPasswordTypeKeys.contains(key)) {
                newDynamicParamsConfig.put(key,
                        firstNonNull(jsDatasourceConnectionConfig.getDynamicParamsValue(key), this.getDynamicParamsValue(key)));
                continue;
            }
            newDynamicParamsConfig.put(key, jsDatasourceConnectionConfig.getDynamicParamsValue(key));
        }
        newJsDatasourceConnectionConfig.put("dynamicParamsConfig", newDynamicParamsConfig);

        // 动态参数定义
        newJsDatasourceConnectionConfig.put("dynamicParamsDef", jsDatasourceConnectionConfig.get("dynamicParamsDef"));

        // 对于动态数据源插件配置的"extra"字段，保持不变。
        if (this.containsKey("extra") || jsDatasourceConnectionConfig.containsKey("extra")) {
            newJsDatasourceConnectionConfig.putIfAbsent("extra", ObjectUtils.firstNonNull(jsDatasourceConnectionConfig.getExtra(), this.getExtra()));
        }
        return newJsDatasourceConnectionConfig;
    }

    /**
     * 对所有密码类型的值进行加密。
     *
     * @param encryptFunc 加密函数
     * @return 已加密的新JsDatasourceConnectionConfig实例
     */
    @Override
    public DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        return doEncryptOrDecrypt(encryptFunc);
    }

    /**
     * 对所有密码类型的值进行解密。
     *
     * @param decryptFunc 解密函数
     * @return 已解密的新JsDatasourceConnectionConfig实例
     */
    @Override
    public DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        return doEncryptOrDecrypt(decryptFunc);
    }

    /**
     * 对所有密码类型的值进行加密或解密。
     *
     * @param encryptOrDecryptFunc 加密或解密函数
     * @return 已加密或解密的新JsDatasourceConnectionConfig实例
     */
    @SuppressWarnings("unchecked")
    private DatasourceConnectionConfig doEncryptOrDecrypt(Function<String, String> encryptOrDecryptFunc) {
        // 加密或解密静态密码值
        Set<String> passwordTypeKeys = getAllStaticPasswordTypeKeys();
        for (Entry<String, Object> entry : this.entrySet()) {
            if (passwordTypeKeys.contains(entry.getKey()) && entry.getValue() instanceof String) {
                this.put(entry.getKey(), encryptOrDecryptFunc.apply((String) entry.getValue()));
            }
        }

        // 加密或解密动态密码值
        Set<String> allDynamicPasswordTypeKeys = getAllDynamicPasswordTypeKeys();
        Map<String, Object> dynamicParamsConfig = (Map<String, Object>) MapUtils.getMap(this, "dynamicParamsConfig", new HashMap<>());
        for (Entry<String, Object> entry : dynamicParamsConfig.entrySet()) {
            if (allDynamicPasswordTypeKeys.contains(entry.getKey()) && entry.getValue() instanceof String) {
                dynamicParamsConfig.put(entry.getKey(), encryptOrDecryptFunc.apply((String) entry.getValue()));
            }
        }

        return this;
    }
}

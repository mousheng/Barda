package com.barda.sdk.util;

import static com.barda.sdk.auth.constants.AuthTypeConstants.FORM;
import static com.barda.sdk.auth.constants.AuthTypeConstants.GITHUB;
import static com.barda.sdk.auth.constants.AuthTypeConstants.GOOGLE;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.barda.sdk.auth.EmailAuthConfig;
import com.barda.sdk.auth.Oauth2SimpleAuthConfig;

import lombok.extern.slf4j.Slf4j;

/**
 * 一个JSON处理的实用工具类。
 * 该类提供了一系列静态方法来对JSON进行序列化和反序列化操作。
 * 它使用了Jackson库来实现JSON的处理。
 */
@Slf4j
public final class JsonUtils {

    /**
     * 一个ObjectMapper实例，用于JSON的序列化和反序列化操作。
     * 它使用了Jackson库的模块来支持Java 8的日期和时间API。
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 注册ParameterNamesModule来支持使用@JsonCreator注解的构造函数
        OBJECT_MAPPER.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        // 注册JavaTimeModule来支持Java 8的日期和时间API
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        // 配置ObjectMapper，在反序列化时忽略未知属性
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 配置ObjectMapper，在序列化时忽略空的Bean
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 注册子类型，以便在反序列化时可以正确地识别子类型
        OBJECT_MAPPER.registerSubtypes(new NamedType(EmailAuthConfig.class, FORM));
        OBJECT_MAPPER.registerSubtypes(new NamedType(Oauth2SimpleAuthConfig.class, GITHUB));
        OBJECT_MAPPER.registerSubtypes(new NamedType(Oauth2SimpleAuthConfig.class, GOOGLE));
    }

    /**
     * 一个空的JSON节点，可以用来作为默认值。
     */
    public static final JsonNode EMPTY_JSON_NODE = createObjectNode();

    /**
     * 一个ObjectWriter实例，用于将对象序列化为JSON字符串。
     */
    private static final ObjectWriter WRITER = OBJECT_MAPPER.writer();

    /**
     * 获取ObjectMapper实例。
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将对象序列化为JSON字符串。
     *
     * @param obj 要序列化的对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        try {
            return WRITER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("fail to print json of class type: {}", obj.getClass().getSimpleName(), e);
            return "";
        }
    }

    /**
     * 将对象序列化为JSON字符串，并指定要包含的属性。
     *
     * @param obj       要序列化的对象
     * @param viewClass 要包含的属性的视图类
     * @return JSON字符串
     */
    public static String toJsonSafely(Object obj, Class<?> viewClass) {
        try {
            return OBJECT_MAPPER.writerWithView(viewClass).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("fail to print json of class type: {}", obj.getClass().getSimpleName(), e);
            return "";
        }
    }

    /**
     * 将对象序列化为JSON字符串。
     *
     * @param obj 要序列化的对象
     * @return JSON字符串
     * @throws JsonProcessingException 如果序列化失败
     */
    public static String toJsonThrows(Object obj) throws JsonProcessingException {
        return WRITER.writeValueAsString(obj);
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象。
     *
     * @param obj     JSON字符串
     * @param tClass  要反序列化的对象的类型
     * @param <T>     要反序列化的对象的类型
     * @return 反序列化得到的对象
     */
    public static <T> T fromJson(String obj, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(obj, tClass);
        } catch (JsonProcessingException e) {
            log.error("fail to print json of class type: {}", obj.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象，并指定默认值。
     *
     * @param obj           JSON字符串
     * @param valueTypeRef  要反序列化的对象的类型引用
     * @param defaultValue  默认值
     * @param <T>           要反序列化的对象的类型
     * @return 反序列化得到的对象，如果反序列化失败，返回默认值
     */
    public static <T> T fromJsonSafely(String obj, TypeReference<T> valueTypeRef, @Nullable T defaultValue) {
        try {
            return OBJECT_MAPPER.readValue(obj, valueTypeRef);
        } catch (JsonProcessingException e) {
            log.error("fail to print json of class type: {}", valueTypeRef.getType().getTypeName(), e);
            return defaultValue;
        }
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象。
     *
     * @param obj     JSON字符串
     * @param tClass  要反序列化的对象的类型
     * @param <T>     要反序列化的对象的类型
     * @return 反序列化得到的对象，如果反序列化失败，返回null
     */
    public static <T> T fromJsonQuietly(String obj, Class<T> tClass) {
        try {
            return OBJECT_MAPPER.readValue(obj, tClass);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 将JSON字符串反序列化为List<Object>。
     *
     * @param obj JSON字符串
     * @return 反序列化得到的List<Object>
     */
    public static List<Object> fromJsonList(String obj) {
        return fromJsonList(obj, Object.class);
    }

    /**
     * 将JSON字符串反序列化为指定类型的List。
     *
     * @param obj     JSON字符串
     * @param tClass  要反序列化的对象的类型
     * @param <T>     要反序列化的对象的类型
     * @return 反序列化得到的List
     */
    public static <T> List<T> fromJsonList(String obj, Class<T> tClass) {
        try {
            CollectionType javaType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, tClass);
            return OBJECT_MAPPER.readValue(obj, javaType);
        } catch (JsonProcessingException e) {
            log.error("fail to print json of class type: {}", obj.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串反序列化为指定类型的Set。
     *
     * @param obj     JSON字符串
     * @param tClass  要反序列化的对象的类型
     * @param <T>     要反序列化的对象的类型
     * @return 反序列化得到的Set
     */
    public static <T> Set<T> fromJsonSet(String obj, Class<T> tClass) {
        try {
            CollectionType javaType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(Set.class, tClass);
            return OBJECT_MAPPER.readValue(obj, javaType);
        } catch (JsonProcessingException e) {
            log.error("fail to print json of class type: {}", obj.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串反序列化为Map<String, Object>。
     *
     * @param obj JSON字符串
     * @return 反序列化得到的Map<String, Object>
     */
    public static Map<String, Object> fromJsonMap(String obj) {
        return fromJsonMap(obj, String.class, Object.class);
    }

    /**
     * 将JSON字符串反序列化为指定类型的Map。
     *
     * @param obj     JSON字符串
     * @param kClass  要反序列化的Map的键的类型
     * @param vClass  要反序列化的Map的值的类型
     * @param <K>     要反序列化的Map的键的类型
     * @param <V>     要反序列化的Map的值的类型
     * @return 反序列化得到的Map
     */
    public static <K, V> Map<K, V> fromJsonMap(String obj, Class<K> kClass, Class<V> vClass) {
        try {
            MapType mapType = OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, kClass, vClass);
            return OBJECT_MAPPER.readValue(obj, mapType);
        } catch (JsonProcessingException e) {
            log.error("fail to print json of class type: {}", obj.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * 将字节数组反序列化为JSON节点。
     *
     * @param body 字节数组
     * @return JSON节点
     * @throws JsonProcessingException 如果反序列化失败
     */
    public static JsonNode readTree(byte[] body) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(new String(body, StandardCharsets.UTF_8));
    }

    /**
     * 将字符串反序列化为JSON节点。
     *
     * @param value 字符串
     * @return JSON节点
     * @throws JsonProcessingException 如果反序列化失败
     */
    public static JsonNode readTree(String value) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(value);
    }

    /**
     * 将对象的值转换为JSON节点。
     *
     * @param value 要转换的对象的值
     * @return JSON节点
     */
    public static JsonNode valueToTree(Object value) {
        return OBJECT_MAPPER.valueToTree(value);
    }

    /**
     * 创建一个空的JSON对象节点。
     *
     * @return 空的JSON对象节点
     */
    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    /**
     * 创建一个空的JSON数组节点。
     *
     * @return 空的JSON数组节点
     */
    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    /**
     * 将JSON节点转换为对象。
     *
     * @param jsonNode JSON节点
     * @return 转换得到的对象
     */
    public static Object jsonNodeToObject(JsonNode jsonNode) {
        try {
            return OBJECT_MAPPER.treeToValue(jsonNode, Object.class);
        } catch (JsonProcessingException e) {
            log.error("jsonNode to object error ", e);
            return null;
        }
    }
}

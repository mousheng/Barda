package com.barda.sdk.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.barda.sdk.plugin.restapi.DataUtils.MultipartFormDataType;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 一个表示属性的类。
 * 它使用Lombok库的@ToString和@EqualsAndHashCode注解来自动生成toString、equals和hashCode方法。
 */
@ToString
@EqualsAndHashCode
public class Property {

    /**
     * 属性的键。
     */
    private final String key;

    /**
     * 属性的值。
     */
    private final String value;

    /**
     * 属性的类型。
     */
    private String type;

    /**
     * 构造函数，创建一个Property实例。
     *
     * @param key 属性的键
     * @param value 属性的值
     */
    @JsonCreator
    public Property(String key, String value) {
        this(key, value, null);
    }

    /**
     * 构造函数，创建一个Property实例。
     *
     * @param key 属性的键
     * @param value 属性的值
     * @param type 属性的类型
     */
    public Property(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    /**
     * 获取属性的键。
     *
     * @return 属性的键
     */
    public String getKey() {
        return key;
    }

    /**
     * 获取属性的值。
     *
     * @return 属性的值
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取属性的类型。
     *
     * @return 属性的类型
     */
    public String getType() {
        return type;
    }

    /**
     * 判断属性是否是多部分文件类型。
     *
     * @return 如果是多部分文件类型，返回true；否则返回false
     */
    public boolean isMultipartFileType() {
        return MultipartFormDataType.FILE.name().equalsIgnoreCase(type);
    }

    /**
     * 设置属性的类型。
     *
     * @param type 属性的类型
     */
    public void setType(String type) {
        this.type = type;
    }
}

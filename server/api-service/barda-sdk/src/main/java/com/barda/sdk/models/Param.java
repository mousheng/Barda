package com.barda.sdk.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 一个表示参数的类，实现了Serializable接口。
 * 它使用Lombok库的注解来自动生成getter、setter、toString和无参构造函数。
 *
 * @author 你的名字
 * @since 2022-01-01
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Param implements Serializable {

    /**
     * 参数的键。
     */
    String key;

    /**
     * 参数的值。
     */
    Object value;

    /**
     * 构造函数，创建一个Param实例。
     *
     * @param key 参数的键
     * @param value 参数的值
     */
    public Param(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 一个静态工厂方法，用于创建一个Param实例。
     *
     * @param key 参数的键
     * @param value 参数的值
     * @return 一个新的Param实例
     */
    public static Param of(String key, Object value) {
        return new Param(key, value);
    }
}

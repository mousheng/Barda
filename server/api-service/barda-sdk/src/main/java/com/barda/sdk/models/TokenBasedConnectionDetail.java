package com.barda.sdk.models;

import java.util.Map;

import org.springframework.data.annotation.Transient;

/**
 * 一个表示基于令牌的连接详细信息的接口。
 * 它继承自Encrypt接口，并使用@Transient注解来标记isStale方法的Transient属性。
 */
public interface TokenBasedConnectionDetail extends Encrypt {

    /**
     * 判断连接详细信息是否过时。
     *
     * @return 如果连接详细信息过时，返回true；否则返回false
     */
    @Transient
    boolean isStale();

    /**
     * 将连接详细信息转换为Map。
     *
     * @return 包含连接详细信息的键值对的Map
     */
    Map<String, Object> toMap();
}

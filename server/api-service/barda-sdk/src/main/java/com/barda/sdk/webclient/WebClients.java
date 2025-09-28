package com.barda.sdk.webclient;

import org.springframework.web.reactive.function.client.WebClient;

/**
 * 一个提供 {@link WebClient} 单例的工具类。
 * 该类使用了懒汉式单例模式来保证只有一个 {@link WebClient} 实例。
 */
public class WebClients {

    /**
     * 单例的 {@link WebClient} 实例。
     */
    private static final WebClient INSTANCE = WebClient.builder().build();

    /**
     * 获取 {@link WebClient} 单例的唯一入口。
     *
     * @return 单例的 {@link WebClient} 实例
     */
    public static WebClient getInstance() {
        return INSTANCE;
    }
}

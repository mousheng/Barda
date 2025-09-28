package com.barda.sdk.destructor;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;

/**
 * 析构器实用类，提供对析构器的注册和执行的功能。
 */
@Slf4j
public class DestructorUtil {

    /**
     * 用于存储注册的销毁回调和描述的队列
     */
    private static final Queue<Pair<Runnable, String>> destroyCallbacks = new LinkedBlockingQueue<>();

    /**
     * 注册销毁回调
     *
     * @param destroyCallback 销毁回调
     * @param des 描述
     */
    public static void register(Runnable destroyCallback, String des) {
        destroyCallbacks.add(Pair.of(destroyCallback, des));
    }

    /**
     * 执行注册的销毁回调
     */
    public static void onDestroy() {
        for (Pair<Runnable, String> pair : destroyCallbacks) {
            try {
                log.info("开始执行销毁回调: {}", pair.getRight());
                pair.getLeft().run();
            } catch (Exception e) {
                log.error("销毁回调执行出错", e);
            }
        }
    }
}

package com.barda.sdk.exception;

import java.io.Serializable;

/**
 * 插件错误接口。
 * 定义了插件中可能发生的错误。
 * 实现了 {@link Serializable} 接口，可以被序列化以便在网络中传输或持久化。
 */
public interface PluginError extends Serializable {

    /**
     * 获取枚举值的名称。
     *
     * @return 枚举值的名称
     */
    String name();

    /**
     * 子类可以重写此方法来指定是否记录详细的错误日志。
     * @return true 如果需要记录详细的错误日志，否则返回 false
     */
    default boolean logVerbose() {
        return false;
    }
}

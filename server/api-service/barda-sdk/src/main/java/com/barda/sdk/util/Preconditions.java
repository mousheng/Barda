package com.barda.sdk.util;

import com.barda.sdk.exception.BizError;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.exception.PluginError;
import com.barda.sdk.exception.PluginException;

/**
 * 前置条件检查器，提供便捷的断言方法来检查条件是否成立。
 */
public class Preconditions {

    /**
     * 检查指定条件是否为 true，如果不为 true，则抛出 {@link BizException}。
     *
     * @param condition 要检查的条件
     * @param errorCode 自定义的业务异常代码
     * @param messageKey 异常信息的键值
     * @param args 格式化异常信息的参数
     * @throws BizException 如果条件不为 true
     */
    public static void check(boolean condition, BizError errorCode, String messageKey, Object... args) {
        if (!condition) {
            throw new BizException(errorCode, messageKey, args);
        }
    }

    /**
     * 检查指定条件是否为 true，如果不为 true，则抛出 {@link PluginException}。
     *
     * @param condition 要检查的条件
     * @param errorCode 自定义的插件异常代码
     * @param messageKey 异常信息的键值
     * @param args 格式化异常信息的参数
     * @throws PluginException 如果条件不为 true
     */
    public static void check(boolean condition, PluginError errorCode, String messageKey, Object... args) {
        if (!condition) {
            throw new PluginException(errorCode, messageKey, args);
        }
    }
}

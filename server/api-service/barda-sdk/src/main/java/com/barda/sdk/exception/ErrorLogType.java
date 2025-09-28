package com.barda.sdk.exception;

/**
 * 错误日志类型枚举。
 *
 * <p>此枚举定义了两种错误日志类型：
 * <ul>
 *     <li>{@link #SIMPLE}: 简单类型，只记录错误信息。</li>
 *     <li>{@link #VERBOSE}: 详细类型，记录错误信息和详细的堆栈跟踪。</li>
 * </ul>
 */
public enum ErrorLogType {

    /**
     * 简单类型，只记录错误信息。
     */
    SIMPLE,

    /**
     * 详细类型，记录错误信息和详细的堆栈跟踪。
     */
    VERBOSE
}


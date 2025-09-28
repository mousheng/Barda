package com.barda.sdk.exception;

import org.slf4j.helpers.MessageFormatter;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;

/**
 * 服务器异常类。
 * 继承自 {@link BaseException}，用于在服务器中发生错误时抛出。
 */
@Slf4j
public class ServerException extends BaseException {

    /**
     * 构造函数，使用指定的消息模板和参数初始化。
     *
     * @param messageTemplate 消息模板
     * @param args 消息参数
     */
    public ServerException(String messageTemplate, Object... args) {
        super(MessageFormatter.arrayFormat(messageTemplate, args, null).getMessage());
    }
}

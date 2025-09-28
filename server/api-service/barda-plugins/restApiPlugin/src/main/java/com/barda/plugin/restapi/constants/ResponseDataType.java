package com.barda.plugin.restapi.constants;

/**
 * 一个表示响应数据类型的枚举。
 * 它定义了一些可能的响应数据类型，包括二进制、图像、文本、JSON和未定义。
 */
public enum ResponseDataType {

    /**
     * 二进制数据类型。
     * 它表示响应数据是二进制格式的。
     */
    BINARY,

    /**
     * 图像数据类型。
     * 它表示响应数据是图像格式的。
     */
    IMAGE,

    /**
     * 文本数据类型。
     * 它表示响应数据是文本格式的。
     */
    TEXT,

    /**
     * JSON数据类型。
     * 它表示响应数据是JSON格式的。
     */
    JSON,

    /**
     * 未定义的数据类型。
     * 它表示响应数据类型未定义。
     */
    UNDEFINED
}

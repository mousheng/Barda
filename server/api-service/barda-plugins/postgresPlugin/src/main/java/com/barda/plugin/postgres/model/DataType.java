/**
 * Copyright 2021 Appsmith Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 */
package com.barda.plugin.postgres.model;

/**
 * PostgreSQL 插件中使用的枚举类型，表示数据类型。
 */

public enum DataType {
    /**
     * 整型
     */
    INTEGER,
    /**
     * 长整型
     */
    LONG,
    /**
     * 浮点数
     */
    FLOAT,
    /**
     * 双精度浮点数
     */
    DOUBLE,
    /**
     * 布尔值
     */
    BOOLEAN,
    /**
     * 日期
     */
    DATE,
    /**
     * 时间
     */
    TIME,
    /**
     * ASCII 字符
     */
    ASCII,
    /**
     * 二进制数据
     */
    BINARY,
    /**
     * 字节数组
     */
    BYTES,
    /**
     * 字符串
     */
    STRING,
    /**
     * 空值
     */
    NULL,
    /**
     * 数组
     */
    ARRAY,
    /**
     * JSON 对象
     */
    JSON_OBJECT,
    /**
     * 时间戳
     */
    TIMESTAMP,
    /**
     * BSON 类型
     */
    BSON
}

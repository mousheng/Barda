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
package com.barda.plugin.mongo.commands;

import static com.barda.plugin.mongo.constants.MongoFieldName.COLLECTION;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.validConfigurationPresentInFormData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.barda.sdk.exception.PluginException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 这是所有 MongoDB 命令的基类。
 * 它包含了所有 MongoDB 命令通用的功能，例如读取和验证集合、定义需要在所有命令中实现的方法。
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class MongoCommand {

    private static final int DEFAULT_VALUE = 8000;

    /**
     * 集合名称。
     */
    private String collection;

    /**
     * 超时时间（毫秒）。
     * 默认值为 8000。
     */
    private int timeoutMs;

    /**
     * 命令类型。
     */
    private String type;

    /**
     * 存储没有在表单数据中配置的字段名称。
     */
    List<String> fieldNamesWithNoConfiguration;

    /**
     * ObjectMapper 实例，用于将 Java 对象转换为 JSON 格式的字符串。
     */
    protected static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造函数，用于从表单数据中提取并初始化 MongoCommand 对象的属性。
     *
     * @param formData 表单数据
     */
    public MongoCommand(Map<String, Object> formData) {

        this.fieldNamesWithNoConfiguration = new ArrayList<>();

        if (validConfigurationPresentInFormData(formData, COLLECTION)) {
            this.collection = (String) formData.get(COLLECTION);
        }

        timeoutMs = MapUtils.getInteger(formData, "timeout", DEFAULT_VALUE);
    }

    /**
     * 验证 MongoCommand 对象是否有效。
     *
     * @return true 表示有效，false 表示无效
     */
    public boolean isValid() {
        if (StringUtils.isBlank(this.collection)) {
            fieldNamesWithNoConfiguration.add(COLLECTION);
            return false;
        }
        return true;
    }

    /**
     * 将 MongoCommand 对象解析为 MongoDB 命令的 Document 表示形式。
     *
     * 子类需要实现此方法来提供具体的命令。
     *
     * @return Document 表示形式的 MongoDB 命令
     * @throws PluginException 如果命令无效
     */
    public Document parseCommand() {
        throw new PluginException(QUERY_EXECUTION_ERROR, "INVALID_MONGODB_OPERATION");
    }

    /**
     * 获取集合名称。
     *
     * @return 集合名称
     */
    public String getCollection() {
        return collection;
    }

    /**
     * 获取超时时间（毫秒）。
     *
     * @return 超时时间（毫秒）
     */
    public int getTimeoutMs() {
        return timeoutMs;
    }

    /**
     * 获取命令类型。
     *
     * @return 命令类型
     */
    public String getType() {
        return type;
    }

    /**
     * 验证输入的字符串是否表示一个数组。
     *
     * @param input 输入的字符串
     * @return true 表示是数组，false 表示不是数组
     */
    protected boolean isArrayStr(String input) {
        input = input.trim();
        return input.startsWith("[") && input.endsWith("]");
    }
}
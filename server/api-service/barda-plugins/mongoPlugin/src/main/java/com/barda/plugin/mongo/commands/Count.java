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

import static com.barda.plugin.mongo.constants.MongoFieldName.COUNT_QUERY;
import static com.barda.plugin.mongo.utils.MongoQueryUtils.parseSafely;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.getValueSafelyFromFormData;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.validConfigurationPresentInFormData;

import java.util.Map;

import org.bson.Document;
import org.pf4j.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * Count 类表示 MongoDB 的 count 命令。
 * 它继承自 MongoCommand 类，并使用 Lombok 注解来生成 getter 和 setter。
 */
@Getter
@Setter
public class Count extends MongoCommand {

    /**
     * 查询条件。
     */
    private String query;

    /**
     * 构造函数，用于从表单数据中提取并初始化 Count 对象的属性。
     *
     * @param formData 表单数据
     */
    public Count(Map<String, Object> formData) {
        super(formData);

        // 从表单数据中提取查询条件
        if (validConfigurationPresentInFormData(formData, COUNT_QUERY)) {
            this.query = (String) getValueSafelyFromFormData(formData, COUNT_QUERY);
        }
    }

    /**
     * 将 Count 对象解析为 MongoDB 命令的 Document 表示形式。
     *
     * @return Document 表示形式的 MongoDB 命令
     */
    @Override
    public Document parseCommand() {
        Document document = new Document();

        // 设置 count 命令的集合
        document.put("count", getCollection());

        // 如果查询条件为空，则使用默认的查询条件
        if (StringUtils.isNullOrEmpty(this.query)) {
            this.query = "{}";
        }

        // 将查询条件添加到命令中
        document.put("query", parseSafely("Query", this.query));

        return document;
    }
}

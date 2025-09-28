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

import static com.barda.plugin.mongo.constants.MongoFieldName.DISTINCT_QUERY;
import static com.barda.plugin.mongo.utils.MongoQueryUtils.parseSafely;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.getValueSafelyFromFormData;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.validConfigurationPresentInFormData;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.barda.plugin.mongo.constants.MongoFieldName;

import lombok.Getter;
import lombok.Setter;

/**
 * Distinct 类表示 MongoDB 的 distinct 命令。
 * 它继承自 MongoCommand 类，并使用 Lombok 注解来生成 getter 和 setter。
 */
@Getter
@Setter
public class Distinct extends MongoCommand {

    /**
     * 查询条件。
     */
    private String query;

    /**
     * 要返回的唯一值的键/字段。
     */
    private String key;

    /**
     * 构造函数，用于从表单数据中提取并初始化 Distinct 对象的属性。
     *
     * @param formData 表单数据
     */
    public Distinct(Map<String, Object> formData) {
        super(formData);

        if (validConfigurationPresentInFormData(formData, DISTINCT_QUERY)) {
            this.query = (String) getValueSafelyFromFormData(formData, DISTINCT_QUERY);
        }

        if (validConfigurationPresentInFormData(formData, MongoFieldName.DISTINCT_KEY)) {
            this.key = (String) getValueSafelyFromFormData(formData, MongoFieldName.DISTINCT_KEY);
        }
    }

    /**
     * 验证 Distinct 对象是否有效。
     *
     * @return true 表示有效，false 表示无效
     */
    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        if (StringUtils.isNotBlank(key)) {
            return true;
        }

        fieldNamesWithNoConfiguration.add("Key/Field");

        return false;
    }

    /**
     * 将 Distinct 对象解析为 MongoDB 命令的 Document 表示形式。
     *
     * @return Document 表示形式的 MongoDB 命令
     */
    @Override
    public Document parseCommand() {
        Document document = new Document();

        document.put("distinct", getCollection());

        if (StringUtils.isBlank(this.query)) {
            this.query = "{}";
        }

        document.put("query", parseSafely("Query", this.query));

        document.put("key", this.key);

        return document;
    }
}

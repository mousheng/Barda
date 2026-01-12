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

import static com.barda.plugin.mongo.constants.MongoFieldName.UPDATE_LIMIT;
import static com.barda.plugin.mongo.constants.MongoFieldName.UPDATE_OPERATION;
import static com.barda.plugin.mongo.constants.MongoFieldName.UPDATE_QUERY;
import static com.barda.plugin.mongo.constants.MongoFieldName.UPDATE_UPSERT;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.getValueSafelyFromFormData;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.validConfigurationPresentInFormData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.barda.plugin.mongo.utils.MongoQueryUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * UpdateMany 类表示 MongoDB 的 updateMany 命令。
 * 它继承自 MongoCommand 类，并使用 Lombok 注解来生成 getter、setter 和无参数的构造函数。
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateMany extends MongoCommand {

    /**
     * 查询条件。
     */
    private String query;

    /**
     * 更新操作。
     */
    private String update;

    /**
     * 是否更新多个文档。
     * 默认为 false，表示只更新一个文档。
     */
    private Boolean multi = Boolean.FALSE;

    /**
     * 是否启用 upsert 模式。
     * 默认为 false，表示如果查询条件不匹配则不插入新文档。
     */
    private Boolean upsert = Boolean.FALSE;

    /**
     * 构造函数，用于从表单数据中提取并初始化 UpdateMany 对象的属性。
     *
     * @param formData 表单数据
     */
    public UpdateMany(Map<String, Object> formData) {
        super(formData);

        if (validConfigurationPresentInFormData(formData, UPDATE_QUERY)) {
            this.query = (String) getValueSafelyFromFormData(formData, UPDATE_QUERY);
        }

        if (validConfigurationPresentInFormData(formData, UPDATE_OPERATION)) {
            this.update = (String) getValueSafelyFromFormData(formData, UPDATE_OPERATION);
        }

        // 默认为 1，表示只更新一个文档。
        if (validConfigurationPresentInFormData(formData, UPDATE_LIMIT)) {
            String limitOption = (String) getValueSafelyFromFormData(formData, UPDATE_LIMIT);
            if ("ALL".equals(limitOption)) {
                this.multi = Boolean.TRUE;
            }
        }

        // 读取 upsert 选项
        if (validConfigurationPresentInFormData(formData, UPDATE_UPSERT)) {
            Object upsertValue = getValueSafelyFromFormData(formData, UPDATE_UPSERT);
            if (upsertValue instanceof Boolean) {
                this.upsert = (Boolean) upsertValue;
            } else if (upsertValue instanceof String) {
                this.upsert = Boolean.parseBoolean((String) upsertValue);
            }
        }
    }

    /**
     * 验证 UpdateMany 对象是否有效。
     *
     * @return true 表示有效，false 表示无效
     */
    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        if (StringUtils.isNotBlank(query) && StringUtils.isNotBlank(update)) {
            return true;
        }

        // 由于数据影响，不添加对 query 的智能默认值
        if (StringUtils.isBlank(query)) {
            fieldNamesWithNoConfiguration.add("Query");
        }
        // 由于数据影响，不添加对 update 的智能默认值
        if (StringUtils.isBlank(update)) {
            fieldNamesWithNoConfiguration.add("Update");
        }
        return false;
    }

    /**
     * 将 UpdateMany 对象解析为 MongoDB 命令的 Document 表示形式。
     *
     * @return Document 表示形式的 MongoDB 命令
     */
    @Override
    public Document parseCommand() {
        Document document = new Document();

        document.put("update", getCollection());

        Document queryDocument = MongoQueryUtils.parseSafely("Query", this.query);
        Document updateDocument = MongoQueryUtils.parseSafely("Update", this.update);

        // upsert 时，将 query 中的简单等值条件合并到 update 中
        if (Boolean.TRUE.equals(upsert)) {
            Document eqFilters = new Document();
            queryDocument.forEach((key, value) -> {
                // 只提取简单等值条件：字段名不以 $ 开头，且值不是 Document（避免包含操作符）
                if (!key.startsWith("$") && !(value instanceof Document)) {
                    eqFilters.put(key, value);
                }
            });

            if (!eqFilters.isEmpty()) {
                boolean hasUpdateOperator = updateDocument.keySet().stream().anyMatch(k -> k.startsWith("$"));
                if (hasUpdateOperator) {
                    // 使用 $setOnInsert 确保只在插入时包含查询条件
                    Document setOnInsert = (Document) updateDocument.getOrDefault("$setOnInsert", new Document());
                    eqFilters.forEach(setOnInsert::putIfAbsent);
                    updateDocument.put("$setOnInsert", setOnInsert);
                } else {
                    // 替换文档模式，直接合并查询条件
                    eqFilters.forEach(updateDocument::putIfAbsent);
                }
            }
        }

        Document update = new Document();
        update.put("q", queryDocument);
        update.put("u", updateDocument);
        update.put("multi", multi);
        update.put("upsert", upsert);

        List<Document> updates = new ArrayList<>();
        updates.add(update);

        document.put("updates", updates);

        return document;
    }
}

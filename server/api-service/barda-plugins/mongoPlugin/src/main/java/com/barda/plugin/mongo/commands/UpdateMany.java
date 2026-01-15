/**
 * Copyright 2021 Appsmith Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 *
 * 行为已对齐 MongoDB Server（Update + upsert）：
 * - upsert 时仅从 query 中提取「等值匹配字段路径」
 * - 忽略任何包含操作符（$gte / $in / $or / $elemMatch / array 等）的条件
 */
@Getter
@Setter
@NoArgsConstructor
public class UpdateMany extends MongoCommand {

    /** 查询条件 */
    private String query;

    /** 更新操作 */
    private String update;

    /** 是否更新多个文档 */
    private Boolean multi = Boolean.FALSE;

    /** 是否启用 upsert */
    private Boolean upsert = Boolean.FALSE;

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
     * MongoDB Server 等价逻辑：
     * 从 query 中提取 upsert 插入文档所需的等值字段（FieldPath → Scalar）
     *
     * 对齐源码：
     * - MatchExpression::getEqualityMatches
     * - UpdateDriver::populateDocumentWithQueryFields
     */
    private void extractMongoUpsertEqualities(
            Document source,
            Document out,
            String prefix
    ) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // $ 开头的一律是操作符，不可能是等值
            if (key.startsWith("$")) {
                continue;
            }

            String fullPath = (prefix == null) ? key : prefix + "." + key;

            if (value instanceof Document) {
                Document subDoc = (Document) value;

                // operator-style document（如 {$gte: 1}）
                boolean isOperatorDoc = subDoc.keySet()
                        .stream()
                        .anyMatch(k -> k.startsWith("$"));

                if (isOperatorDoc) {
                    // MongoDB Server：该路径无等值匹配
                    continue;
                }

                // 嵌套文档，继续向下构造 FieldPath
                extractMongoUpsertEqualities(subDoc, out, fullPath);
                continue;
            }

            // MongoDB Server：数组等值不参与 upsert 插入构造
            if (value instanceof List) {
                continue;
            }

            // 标量等值匹配
            out.put(fullPath, value);
        }
    }

    @Override
    public Document parseCommand() {
        Document document = new Document();
        document.put("update", getCollection());

        Document queryDocument = MongoQueryUtils.parseSafely("Query", this.query);
        Document updateDocument = MongoQueryUtils.parseSafely("Update", this.update);

        // ===== MongoDB Server 行为：upsert 等值字段合并 =====
        if (Boolean.TRUE.equals(upsert)) {
            Document eqFilters = new Document();
            extractMongoUpsertEqualities(queryDocument, eqFilters, null);

            if (!eqFilters.isEmpty()) {
                boolean hasUpdateOperator = updateDocument.keySet()
                        .stream()
                        .anyMatch(k -> k.startsWith("$"));

                if (hasUpdateOperator) {
                    // 使用 $setOnInsert，仅在插入时生效
                    Document setOnInsert =
                            (Document) updateDocument.getOrDefault("$setOnInsert", new Document());

                    eqFilters.forEach(setOnInsert::putIfAbsent);
                    updateDocument.put("$setOnInsert", setOnInsert);
                } else {
                    // replacement-style update
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

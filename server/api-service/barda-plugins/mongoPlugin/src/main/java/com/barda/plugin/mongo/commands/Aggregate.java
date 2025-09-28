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

import static com.barda.plugin.mongo.constants.MongoFieldName.AGGREGATE_LIMIT;
import static com.barda.plugin.mongo.constants.MongoFieldName.AGGREGATE_PIPELINE;
import static com.barda.plugin.mongo.utils.MongoQueryUtils.parseSafely;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.getValueSafelyFromFormData;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.bson.BsonArray;
import org.bson.Document;
import org.bson.json.JsonParseException;
import org.pf4j.util.StringUtils;

import com.google.common.collect.ImmutableList;
import com.barda.sdk.exception.PluginException;

import lombok.Getter;
import lombok.Setter;

/**
 * Aggregate 类表示 MongoDB 的 aggregate 命令。
 * 它继承自 MongoCommand 类，并使用 Lombok 注解来生成 getter 和 setter。
 */
@Getter
@Setter
public class Aggregate extends MongoCommand {

    /**
     * 管道操作的数组。
     */
    private String pipeline;

    /**
     * 要返回的文档数量的上限。
     */
    private int limit;

    /**
     * 构造函数，用于从表单数据中提取并初始化 Aggregate 对象的属性。
     *
     * @param formData 表单数据
     */
    public Aggregate(Map<String, Object> formData) {
        super(formData);

        // 从表单数据中提取管道操作的数组
        if (getValueSafelyFromFormData(formData, AGGREGATE_PIPELINE) instanceof String pipelineStr) {
            this.pipeline = pipelineStr;
        }

        // 从表单数据中提取返回的文档数量的上限
        if (getValueSafelyFromFormData(formData, AGGREGATE_LIMIT) instanceof String limitStr) {
            if (isBlank(limitStr)) {
                limit = Integer.MAX_VALUE;
            } else {
                limit = NumberUtils.toInt(limitStr, 0);
                if (limit <= 0) {
                    throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_LIMIT_CONFIG");
                }
            }
        }
    }

    /**
     * 验证 Aggregate 对象是否有效。
     *
     * @return true 表示有效，false 表示无效
     */
    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        if (StringUtils.isNullOrEmpty(pipeline)) {
            fieldNamesWithNoConfiguration.add("Array of Pipelines");
            return false;
        }

        return true;
    }

    /**
     * 将 Aggregate 对象解析为 MongoDB 命令的 Document 表示形式。
     *
     * @return Document 表示形式的 MongoDB 命令
     */
    @Override
    public Document parseCommand() {
        Document commandDocument = new Document();

        commandDocument.put("aggregate", getCollection());
        commandDocument.put("pipeline", parsePipeline());
        commandDocument.put("cursor", parseSafely("cursor", "{batchSize: " + limit + "}"));

        return commandDocument;
    }

    /**
     * 解析管道操作的数组。
     *
     * @return 解析后的管道操作的数组
     */
    private Object parsePipeline() {
        if (isArrayStr(pipeline)) {
            try {
                BsonArray arrayListFromInput = BsonArray.parse(pipeline);
                if (arrayListFromInput.isEmpty()) {
                    return "[]";
                }
                return arrayListFromInput;
            } catch (JsonParseException e) {
                throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_MONGODB_BSON_ARRAY_FORMAT");
            }
        }

        Document pipeline = parseSafely("Array of Pipelines", this.pipeline);
        return ImmutableList.of(pipeline);
    }
}

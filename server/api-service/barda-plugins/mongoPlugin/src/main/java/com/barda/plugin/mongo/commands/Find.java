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

import static com.barda.plugin.mongo.constants.MongoFieldName.FIND_LIMIT;
import static com.barda.plugin.mongo.constants.MongoFieldName.FIND_PROJECTION;
import static com.barda.plugin.mongo.constants.MongoFieldName.FIND_QUERY;
import static com.barda.plugin.mongo.constants.MongoFieldName.FIND_SKIP;
import static com.barda.plugin.mongo.constants.MongoFieldName.FIND_SORT;
import static com.barda.plugin.mongo.utils.MongoQueryUtils.parseSafely;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.getValueSafelyFromFormData;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.validConfigurationPresentInFormData;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.Document;

import com.barda.sdk.exception.PluginException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Find 类表示 MongoDB 的 find 命令。
 * 它继承自 MongoCommand 类，并使用 Lombok 注解来生成 getter、setter 和无参数的构造函数。
 */
@Getter
@Setter
@NoArgsConstructor
public class Find extends MongoCommand {

    /**
     * 查询条件。
     */
    private String query;

    /**
     * 排序条件。
     */
    private String sort;

    /**
     * 投影条件。
     */
    private String projection;

    /**
     * 限制返回的文档数量。
     */
    private String limit;

    /**
     * 跳过的文档数量。
     */
    private String skip;

    /**
     * 构造函数，用于从表单数据中提取并初始化 Find 对象的属性。
     *
     * @param formData 表单数据
     */
    public Find(Map<String, Object> formData) {
        super(formData);

        if (validConfigurationPresentInFormData(formData, FIND_QUERY)) {
            this.query = (String) getValueSafelyFromFormData(formData, FIND_QUERY);
        }

        if (validConfigurationPresentInFormData(formData, FIND_SORT)) {
            this.sort = (String) getValueSafelyFromFormData(formData, FIND_SORT);
        }

        if (validConfigurationPresentInFormData(formData, FIND_PROJECTION)) {
            this.projection = (String) getValueSafelyFromFormData(formData, FIND_PROJECTION);
        }

        if (validConfigurationPresentInFormData(formData, FIND_LIMIT)) {
            this.limit = (String) getValueSafelyFromFormData(formData, FIND_LIMIT);
        }

        if (validConfigurationPresentInFormData(formData, FIND_SKIP)) {
            this.skip = (String) getValueSafelyFromFormData(formData, FIND_SKIP);
        }
    }

    /**
     * 将 Find 对象解析为 MongoDB 命令的 Document 表示形式。
     *
     * @return Document 表示形式的 MongoDB 命令
     */
    @Override
    public Document parseCommand() {
        Document document = new Document();

        if (StringUtils.isBlank(this.query)) {
            this.query = "{}";
        }

        document.put("find", getCollection());

        document.put("filter", parseSafely("Query", this.query));

        if (StringUtils.isNotBlank(this.sort)) {
            document.put("sort", parseSafely("Sort", this.sort));
        }

        if (StringUtils.isNotBlank(this.projection)) {
            document.put("projection", parseSafely("Projection", this.projection));
        }

        setLimitAndBatchSize(this.limit, document);

        if (StringUtils.isNotBlank(this.skip)) {
            document.put("skip", Long.parseLong(this.skip));
        }

        return document;
    }

    /**
     * 设置 limit 和 batchSize。
     *
     * @param limitStr 限制返回的文档数量的字符串表示
     * @param document 要修改的 Document 对象
     */
    private void setLimitAndBatchSize(String limitStr, Document document) {
        if (StringUtils.isBlank(limitStr)) {
            document.put("batchSize", Integer.MAX_VALUE);
            return;
        }

        int limit = NumberUtils.toInt(limitStr, 0);
        if (limit <= 0) {
            throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_LIMIT_CONFIG");
        }

        document.put("limit", limit);
        document.put("batchSize", limit);
    }
}
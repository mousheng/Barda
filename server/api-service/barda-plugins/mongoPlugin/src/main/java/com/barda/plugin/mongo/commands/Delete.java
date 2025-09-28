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

import static com.barda.plugin.mongo.constants.MongoFieldName.DELETE_LIMIT;
import static com.barda.plugin.mongo.constants.MongoFieldName.DELETE_QUERY;
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
 * Delete 类表示 MongoDB 的 delete 命令。
 * 它继承自 MongoCommand 类，并使用 Lombok 注解来生成 getter、setter 和无参数的构造函数。
 */
@Getter
@Setter
@NoArgsConstructor
public class Delete extends MongoCommand {

    /**
     * 查询条件。
     */
    private String query;

    /**
     * 要删除的文档数量。
     * 0 表示删除所有匹配的文档，1 表示删除单个匹配的文档。
     */
    private Integer limit = 1;

    /**
     * 构造函数，用于从表单数据中提取并初始化 Delete 对象的属性。
     *
     * @param formData 表单数据
     */
    public Delete(Map<String, Object> formData) {
        super(formData);

        if (validConfigurationPresentInFormData(formData, DELETE_QUERY)) {
            this.query = (String) getValueSafelyFromFormData(formData, DELETE_QUERY);
        }

        if (validConfigurationPresentInFormData(formData, DELETE_LIMIT)) {
            String limitOption = (String) getValueSafelyFromFormData(formData, DELETE_LIMIT);
            if ("ALL".equals(limitOption)) {
                this.limit = 0;
            }
        }
    }

    /**
     * 验证 Delete 对象是否有效。
     *
     * @return true 表示有效，false 表示无效
     */
    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        if (StringUtils.isNotBlank(query)) {
            return true;
        }

        // 对于 query，不提供智能默认值以免对数据造成不良影响
        fieldNamesWithNoConfiguration.add("Query");
        return false;
    }

    /**
     * 将 Delete 对象解析为 MongoDB 命令的 Document 表示形式。
     *
     * @return Document 表示形式的 MongoDB 命令
     */
    @Override
    public Document parseCommand() {
        Document document = new Document();

        document.put("delete", getCollection());

        Document queryDocument = MongoQueryUtils.parseSafely("Query", this.query);

        Document delete = new Document();
        delete.put("q", queryDocument);
        delete.put("limit", this.limit);

        List<Document> deletes = new ArrayList<>();
        deletes.add(delete);

        document.put("deletes", deletes);

        return document;
    }
}
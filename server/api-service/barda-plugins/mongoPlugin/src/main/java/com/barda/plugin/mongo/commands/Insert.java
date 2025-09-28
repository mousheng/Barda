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

import static com.barda.plugin.mongo.constants.MongoFieldName.INSERT_DOCUMENT;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.getValueSafelyFromFormData;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.validConfigurationPresentInFormData;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.BsonArray;
import org.bson.Document;
import org.bson.json.JsonParseException;

import com.barda.plugin.mongo.utils.MongoQueryUtils;
import com.barda.sdk.exception.PluginException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Insert 类表示 MongoDB 的 insert 命令。
 * 它继承自 MongoCommand 类，并使用 Lombok 注解来生成 getter、setter 和无参数的构造函数。
 */
@Getter
@Setter
@NoArgsConstructor
public class Insert extends MongoCommand {

    /**
     * 要插入的文档。
     */
    private String documents;

    /**
     * 构造函数，用于从表单数据中提取并初始化 Insert 对象的属性。
     *
     * @param formData 表单数据
     */
    public Insert(Map<String, Object> formData) {
        super(formData);

        if (validConfigurationPresentInFormData(formData, INSERT_DOCUMENT)) {
            this.documents = (String) getValueSafelyFromFormData(formData, INSERT_DOCUMENT);
        }
    }

    /**
     * 验证 Insert 对象是否有效。
     *
     * @return true 表示有效，false 表示无效
     */
    @Override
    public boolean isValid() {
        if (!super.isValid()) {
            return false;
        }

        if (StringUtils.isNotBlank(documents)) {
            return true;
        }

        fieldNamesWithNoConfiguration.add("Documents");
        return false;
    }

    /**
     * 将 Insert 对象解析为 MongoDB 命令的 Document 表示形式。
     *
     * @return Document 表示形式的 MongoDB 命令
     */
    @Override
    public Document parseCommand() {
        Document commandDocument = new Document();

        commandDocument.put("insert", getCollection());

        if (isArrayStr(documents)) {
            try {
                BsonArray arrayListFromInput = BsonArray.parse(documents);
                if (arrayListFromInput.isEmpty()) {
                    commandDocument.put("documents", "[]");
                } else {
                    commandDocument.put("documents", arrayListFromInput);
                }
            } catch (JsonParseException e) {
                throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_JSON_ARRAY_FORMAT");
            }
        } else {
            // 命令期望文档以数组的形式发送。解析并创建一个单元素数组
            Document document = MongoQueryUtils.parseSafely("Documents", documents);
            ArrayList<Document> documentArrayList = new ArrayList<>();
            documentArrayList.add(document);

            commandDocument.put("documents", documentArrayList);
        }

        return commandDocument;
    }
}
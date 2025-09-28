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

// copied and adapted for mongo result parsing

package com.barda.plugin.mongo.utils;

import static com.barda.plugin.mongo.constants.MongoFieldName.COMMAND_TYPE;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.util.JsonUtils.createObjectNode;
import static com.barda.sdk.util.JsonUtils.readTree;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.bson.Document;
import org.bson.json.JsonParseException;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.barda.plugin.mongo.commands.Aggregate;
import com.barda.plugin.mongo.commands.Count;
import com.barda.plugin.mongo.commands.Delete;
import com.barda.plugin.mongo.commands.Distinct;
import com.barda.plugin.mongo.commands.Find;
import com.barda.plugin.mongo.commands.Insert;
import com.barda.plugin.mongo.commands.MongoCommand;
import com.barda.plugin.mongo.commands.UpdateMany;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceStructure;

/**
 * MongoQueryUtils是提供MongoDB查询相关功能的实用类。
 *
 * <p>
 * MongoQueryUtils包含了执行MongoDB查询所需的各种方法，
 * 例如解析JSON、生成数据结构、执行原始命令等。
 * </p>
 */
public class MongoQueryUtils {

    public static final String N_MODIFIED = "nModified";

    private static final String VALUE = "value";

    private static final String VALUES = "values";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());


    /**
     * 安全地解析JSON并返回Document。
     *
     * @param fieldName 字段名称
     * @param input 输入的JSON字符串
     * @return Document
     * @throws PluginException 如果输入的JSON格式不正确
     */
    public static Document parseSafely(String fieldName, String input) {
        try {
            return Document.parse(input);
        } catch (JsonParseException e) {
            throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_JSON_FORMAT", fieldName);
        }
    }

    /**
     * 检查是否是原始命令。
     *
     * @param formData 表单数据
     * @return true表示是原始命令，false表示不是原始命令
     */
    public static boolean isRawCommand(Map<String, Object> formData) {
        String command = (String) formData.getOrDefault(COMMAND_TYPE, null);
        return "RAW".equalsIgnoreCase(command);
    }

    /**
     * 将MongoDB表单输入转换为原始命令。
     *
     * @param formData 表单数据
     * @return MongoCommand
     * @throws PluginException 如果命令类型无效或参数配置不正确
     */
    public static MongoCommand convertMongoFormInputToRawCommand(Map<String, Object> formData) {

        // Parse the commands into raw appropriately
        // 适当地将命令解析为原始命令
        String commandType = (String) formData.getOrDefault(COMMAND_TYPE, "");
        MongoCommand command = switch (commandType.toUpperCase()) {
            case "INSERT" -> new Insert(formData);
            case "FIND" -> new Find(formData);
            case "UPDATE" -> new UpdateMany(formData);
            case "DELETE" -> new Delete(formData);
            case "COUNT" -> new Count(formData);
            case "DISTINCT" -> new Distinct(formData);
            case "AGGREGATE" -> new Aggregate(formData);
            default -> throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_MONGODB_REQUEST", commandType);
        };
        if (!command.isValid()) {
            throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_PARAM_CONFIG_PLZ_CHECK",
                    command.getFieldNamesWithNoConfiguration());
        }

        return command;
    }

    /**
     * 为集合生成模板和结构。
     *
     * @param document 集合的示例文档
     * @param columns 集合的列结构
     */
    public static void generateTemplatesAndStructureForACollection(Document document,
            ArrayList<DatasourceStructure.Column> columns) {
        String filterFieldName = null;
        for (Map.Entry<String, Object> entry : document.entrySet()) {
            final String name = entry.getKey();
            final Object value = entry.getValue();
            String type;
            boolean isAutogenerated = false;

            if (value instanceof Integer) {
                type = "Integer";
            } else if (value instanceof Long) {
                type = "Long";
            } else if (value instanceof Double) {
                type = "Double";
            } else if (value instanceof Decimal128) {
                type = "BigDecimal";
            } else if (value instanceof String) {
                type = "String";
                if (filterFieldName == null || filterFieldName.compareTo(name) > 0) {
                    filterFieldName = name;
                }
            } else if (value instanceof ObjectId) {
                type = "ObjectId";
                isAutogenerated = true;
            } else if (value instanceof Collection) {
                type = "Array";
            } else if (value instanceof Date) {
                type = "Date";
            } else {
                type = "Object";
            }

            columns.add(new DatasourceStructure.Column(name, type, null, isAutogenerated));
        }

        columns.sort(Comparator.naturalOrder());
    }

    /**
     * 对文本进行URL编码。
     *
     * @param text 要编码的文本
     * @return 编码后的文本
     */
    public static String urlEncode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    /**
     * 解析查询结果的主体并返回JsonNode。
     *
     * @param outputJson 查询结果的JSON对象
     * @return JsonNode
     * @throws JsonProcessingException 如果JSON处理时出错
     */
    public static JsonNode parseResultBody(JSONObject outputJson) throws JsonProcessingException {
        /*
         对于`findAndModify`命令，我们不获取所做的修改的计数。相反，
         我们要么得到修改的新值，要么得到预修改的旧值（取决于
         `new`字段中的命令）。让我们返回该值以供用户使用。
         */
        if (outputJson.has(VALUE)) {
            return readTree(cleanUp(new JSONObject().put(VALUE, outputJson.get(VALUE))).toString());
        }

        /*
         JSON中包含键"cursor"时，find命令被执行，并且有1或多个结果。
         在find命令中，如果没有结果，则此键不在结果JSON中。
         */
        if (outputJson.has("cursor")) {
            JSONArray outputResult = (JSONArray) cleanUp(
                    outputJson.getJSONObject("cursor").getJSONArray("firstBatch"));
            return readTree(outputResult.toString());
        }

        /*
         JSON中包含键"n"时，insert/update命令被执行。"n"对于update
         表示所选的文档数。"n"在insert的情况下表示插入的文档数。
         */
        if (outputJson.has("n")) {
            JSONObject body = new JSONObject().put("n", outputJson.getBigInteger("n"));
            return readTree(body.toString());
        }

        /*
         JSON键包含键"nModified"时，update命令被执行。
         这表示所更新的文档数。
         */
        if (outputJson.has(N_MODIFIED)) {
            JSONObject body = new JSONObject().put(N_MODIFIED, outputJson.getBigInteger(N_MODIFIED));
            return readTree(body.toString());
        }

        /*
         JSON中包含键"values"时，distinct命令被使用。
         */
        if (outputJson.has(VALUES)) {
            JSONArray outputResult = (JSONArray) cleanUp(
                    outputJson.getJSONArray(VALUES));

            ObjectNode resultNode = createObjectNode();

            // 创建一个JSON结构，将结果存储在一个键中，以遵守
            // 服务器-客户端约定的仅发送数组的对象结果。
            resultNode.putArray(VALUES)
                    .addAll((ArrayNode) readTree(outputResult.toString()));

            return readTree(resultNode.toString());
        }

        return null;
    }

    private static Object cleanUp(Object object) {
        if (object instanceof JSONObject jsonObject) {
            final boolean isSingleKey = jsonObject.keySet().size() == 1;

            if (isSingleKey && "$numberLong".equals(jsonObject.keys().next())) {
                return jsonObject.getBigInteger("$numberLong");

            } else if (isSingleKey && "$oid".equals(jsonObject.keys().next())) {
                return jsonObject.getString("$oid");

            } else if (isSingleKey && "$date".equals(jsonObject.keys().next())) {
                Instant instant;
                if (jsonObject.get("$date") instanceof Long millis) {
                    instant = Instant.ofEpochMilli(millis);
                } else {
                    instant = Instant.parse(jsonObject.getString("$date"));
                }
                return FORMATTER.format(instant);
            } else if (isSingleKey && "$numberDecimal".equals(jsonObject.keys().next())) {
                return new BigDecimal(jsonObject.getString("$numberDecimal"));

            } else {
                for (String key : new HashSet<>(jsonObject.keySet())) {
                    jsonObject.put(key, cleanUp(jsonObject.get(key)));
                }

            }

        } else if (object instanceof JSONArray) {
            Collection<Object> cleaned = new ArrayList<>();

            for (Object child : (JSONArray) object) {
                cleaned.add(cleanUp(child));
            }

            return new JSONArray(cleaned);

        }

        return object;
    }

}

package com.barda.sdk.util;

import static com.barda.sdk.exception.PluginCommonError.JSON_PARSE_ERROR;
import static com.barda.sdk.util.JsonUtils.EMPTY_JSON_NODE;
import static com.barda.sdk.util.JsonUtils.createArrayNode;
import static com.barda.sdk.util.JsonUtils.createObjectNode;
import static com.barda.sdk.util.JsonUtils.valueToTree;
import static com.barda.sdk.util.MustacheHelper.isMustacheToken;
import static com.barda.sdk.util.MustacheHelper.removeCurlyBraces;
import static com.barda.sdk.util.MustacheHelper.tokenize;
import static com.barda.sdk.util.StreamUtils.toMapNullFriendly;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.replace;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.barda.sdk.exception.PluginException;

import tv.twelvetone.json.Json;
import tv.twelvetone.json.JsonObject.Member;
import tv.twelvetone.json.JsonValue;

/**
 * RjsonMustacheParser 类提供 JSON 字符串和参数的 Mustache 模板渲染功能。
 * 该类使用 Rjson 库来解析 JSON 字符串，并使用 MustacheHelper 类来处理 Mustache 模板。
 */
class RjsonMustacheParser {

    /**
     * 用于在 Mustache 模板中替换的标记。
     */
    private static final String REPLACE_TOKEN = "#replace";

    /**
     * 渲染 Mustache 模板并返回 JSON 字符串。
     *
     * @param jsonStr 要渲染的 JSON 字符串
     * @param paramMap 包含 Mustache 模板中使用的参数
     * @return 渲染后的 JSON 字符串
     */
    public static String renderMustacheJsonString(String jsonStr, Map<String, Object> paramMap) {
        return renderMustacheJson(jsonStr, paramMap).toString();
    }

    /**
     * 渲染 Mustache 模板并返回 JSON 节点。
     *
     * @param jsonStr 要渲染的 JSON 字符串
     * @param paramMap 包含 Mustache 模板中使用的参数
     * @return 渲染后的 JSON 节点
     */
    public static JsonNode renderMustacheJson(String jsonStr, Map<String, ?> paramMap) {
        if (StringUtils.isBlank(jsonStr)) {
            return EMPTY_JSON_NODE;
        }

        List<String> tokens = tokenize(jsonStr.trim());
        // handle cases like " {{ map }} " / "2022-05-05 11:12:13"
        if (tokens.size() == 1) {
            String oneTokenStr = tokens.get(0);
            if (isMustacheToken(oneTokenStr)) {
                Object value = paramMap.get(removeCurlyBraces(oneTokenStr));
                return convertToJsonNode(value);
            }

            try {
                return traverse(RjsonParser.parse(oneTokenStr), Map.of());
            } catch (Throwable e) {
                // return as a textNode if fails to parse
                return TextNode.valueOf(oneTokenStr);
            }
        }

        Map<String, String> tokenReplaceMap = new HashMap<>();

        String escapeEvaluatedTokens = escapeEvaluatedTokens(tokenReplaceMap, tokens);

        Map<String, Object> tokenReplaceValueMap = getTokenReplaceValueMap(paramMap, tokenReplaceMap);

        JsonValue json;
        try {
            json = RjsonParser.parse(escapeEvaluatedTokens);
        } catch (Throwable e) {
            throw new PluginException(JSON_PARSE_ERROR, "JSON_PARSE_ERROR", escapeEvaluatedTokens, e.getMessage());
        }

        return traverse(json, tokenReplaceValueMap);
    }

    /**
     * 根据给定的参数映射和令牌替换映射生成一个新的映射。
     *
     * <p>
     * 该方法从 `paramMap` 中提取值，并根据 `tokenReplaceMap` 提供的键值对将其映射到新的键。具体来说，`tokenReplaceMap`
     * 的键用于查找 `paramMap` 中的相应值，而 `tokenReplaceMap` 的值将作为新映射中的键。
     * </p>
     *
     * @param paramMap 一个包含原始键值对的映射，键为 `String` 类型，值为 `Object` 类型。
     * @param tokenReplaceMap 一个包含键值对的映射，定义了如何将 `paramMap` 中的键替换为新的键。键为 `String` 类型，
     *                        表示 `paramMap` 中的键，值为 `String` 类型，表示新映射中的键。
     * @return 一个新的 `Map`，其中包含从 `paramMap` 中提取并根据 `tokenReplaceMap` 替换后的键值对。
     */
    private static Map<String, Object> getTokenReplaceValueMap(Map<String, ?> paramMap, Map<String, String> tokenReplaceMap) {
        // 将 paramMap 中的键进行去空格处理，并保留原始值
        Map<String, Object> trimmedValueMap = paramMap.entrySet()
                .stream()
                .collect(toMapNullFriendly(it -> it.getKey().trim(), Entry::getValue, (a, b) -> b));

        // 根据 tokenReplaceMap 中的映射关系，创建新的映射
        return tokenReplaceMap.entrySet()
                .stream()
                .collect(toMapNullFriendly(Entry::getValue, entry -> trimmedValueMap.get(entry.getKey())));
    }


    /**
     * 遍历 JSON 值并将其转换为 JSON 节点。
     *
     * @param jsonValue 要转换的 JSON 值
     * @param paramMap 包含 Mustache 模板中使用的参数
     * @return 转换后的 JSON 节点
     */
    private static JsonNode traverse(JsonValue jsonValue, Map<String, Object> paramMap) {

        if (jsonValue.isBoolean()) {
            return BooleanNode.valueOf(jsonValue.asBoolean());
        }

        if (jsonValue.isNull()) {
            return NullNode.getInstance();
        }

        if (jsonValue.isNumber()) {
            String s = jsonValue.toString();
            Number number = NumberUtils.createNumber(s);
            return tryGetNumberNode(number);
        }

        if (jsonValue.isArray()) {

            ArrayNode newArrayNode = createArrayNode();

            for (JsonValue node : jsonValue.asArray()) {
                newArrayNode.add(traverse(node, paramMap));
            }

            return newArrayNode;
        }

        if (jsonValue.isObject()) {
            var objectNode = createObjectNode();
            for (Member member : jsonValue.asObject()) {
                String name = member.getName();
                JsonValue value = member.getValue();

                String updatedName = tryResolveAsString(name, paramMap);
                objectNode.set(updatedName, traverse(value, paramMap));
            }
            return objectNode;
        }

        return tryResolve(jsonValue, paramMap, false);
    }

    /**
     * 尝试将给定的输入字符串解析为 JSON，并根据提供的参数映射替换其中的变量，然后返回解析后的字符串值。
     *
     * <p>
     * 该方法使用 `tryResolve` 方法对输入字符串进行解析和变量替换。解析后的 JSON 节点的文本值作为结果返回。
     * </p>
     *
     * @param input 一个包含 JSON 字符串的输入，可能包含变量占位符。
     * @param paramMap 一个包含变量名称和对应值的映射，键为 `String` 类型，值为 `Object` 类型。用于替换输入字符串中的变量。
     * @return 解析并替换变量后的字符串值。如果解析失败或无法找到对应的文本值，返回 `null`。
     */
    private static String tryResolveAsString(String input, Map<String, Object> paramMap) {
        // 使用 Json.INSTANCE.value 方法将输入字符串解析为 JSON 节点
        JsonNode jsonNode = tryResolve(Json.INSTANCE.value(input), paramMap, true);
        // 返回解析后的 JSON 节点的文本值
        return jsonNode.textValue();
    }

    private static JsonNode tryResolve(JsonValue jsonValue, Map<String, Object> paramMap, boolean toStringType) {

        String input = jsonValue.asString().trim();
        if (isBlank(input)) {
            return TextNode.valueOf(input);
        }

        var checkStringResult = checkString(input);
        if (checkStringResult.isRawStr()) {
            return TextNode.valueOf(input);
        }

        if (toStringType || checkStringResult.isQuotedStr()) {
            return TextNode.valueOf(MustacheHelper.renderMustacheString(checkStringResult.result(), paramMap));
        }

        List<String> tokenize = tokenize(input);
        if (tokenize.isEmpty()) {
            return TextNode.valueOf(input);
        }

        if (tokenize.size() == 1) {
            String token = tokenize.get(0);
            if (token.startsWith("{{") && token.endsWith("}}")) {
                Object mustacheValue = paramMap.get(token.substring(2, token.length() - 2).trim());
                return convertToJsonNode(mustacheValue);
            }
        }

        return TextNode.valueOf(MustacheHelper.renderMustacheTokens(tokenize, paramMap));
    }

    /**
     * 将 Mustache 值转换为 JSON 节点。
     *
     * @param mustacheValue Mustache 值
     * @return 转换后的 JSON 节点
     */
    private static JsonNode convertToJsonNode(Object mustacheValue) {
        if (mustacheValue == null) {
            return NullNode.getInstance();
        }

        if (mustacheValue instanceof Collection<?> || mustacheValue instanceof Map<?, ?>) {
            return valueToTree(mustacheValue);
        }

        if (mustacheValue instanceof Number) {
            return tryGetNumberNode(mustacheValue);
        }

        if (mustacheValue instanceof Boolean) {
            return BooleanNode.valueOf((boolean) mustacheValue);
        }

        return TextNode.valueOf(mustacheValue.toString());
    }

    /**
     * 获取数字类型的 JSON 节点。
     *
     * @param number 数字值
     * @return 数字类型的 JSON 节点
     */
    @Nullable
    private static JsonNode tryGetNumberNode(Object number) {
        if (number instanceof Integer) {
            return IntNode.valueOf((int) number);
        }

        if (number instanceof Long) {
            return LongNode.valueOf((long) number);
        }

        if (number instanceof Float) {
            return FloatNode.valueOf((float) number);
        }

        if (number instanceof Double) {
            return DoubleNode.valueOf((double) number);
        }
        throw new PluginException(JSON_PARSE_ERROR, "JSON_PARSE_ERROR", number, "unknown number node: " + number.getClass().getSimpleName());
    }

    /**
     * 检查字符串的类型。
     *
     * @param str 要检查的字符串
     * @return 字符串的类型
     */
    private static StringCheckResult checkString(String str) {
        String escapedLeftPar = "\\{\\{";
        String escapedRightPar = "\\}\\}";
        if (str.contains(escapedLeftPar) && str.contains(escapedRightPar)) {
            var newStr = replace(str, escapedLeftPar, "{{");
            newStr = replace(newStr, escapedRightPar, "}}");
            return new StringCheckResult(2, newStr);
        }

        if (str.contains("{{") && str.contains("}}")) {
            return new StringCheckResult(1, str);
        }
        return new StringCheckResult(0, str);
    }

    /**
     * 记录字符串的类型。
     */
    private record StringCheckResult(int type, String result) {

        public boolean isRawStr() {
            return type == 0;
        }

        public boolean isQuotedStr() {
            return type == 2;
        }
    }


    /**
     * 转义 Mustache 模板中的标记。
     *
     * @param tokenReplaceMap 标记和值对
     * @param tokens 标记列表
     * @return 转义后的字符串
     */
    @Nonnull
    private static String escapeEvaluatedTokens(Map<String, String> tokenReplaceMap, List<String> tokens) {

        StringBuilder result = new StringBuilder();

        AtomicInteger replaceCount = new AtomicInteger();

        for (String token : tokens) {
            if (MustacheHelper.isMustacheToken(token)) {
                String tokenKey = token.substring(2, token.length() - 2).trim();

                String replaceToken = tokenReplaceMap.computeIfAbsent(tokenKey, ignore -> generateToken(replaceCount));
                result.append("\\{\\{").append(replaceToken).append("\\}\\}");
                continue;
            }
            result.append(token);
        }

        return result.toString();
    }

    /**
     * 生成一个唯一的标记。
     *
     * @param replaceCount 标记计数器
     * @return 唯一的标记
     */
    @Nonnull
    private static String generateToken(AtomicInteger replaceCount) {
        return REPLACE_TOKEN + replaceCount.getAndIncrement();
    }

}

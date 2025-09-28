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

// copied and adapted for mustache parsing

package com.barda.sdk.util;

import static com.google.common.collect.Lists.newArrayList;
import static com.barda.sdk.exception.PluginCommonError.SQL_IN_OPERATOR_PARSE_ERROR;
import static com.barda.sdk.util.JsonUtils.toJson;
import static com.barda.sdk.util.StreamUtils.collectMap;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substring;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.text.StringEscapeUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.barda.sdk.exception.PluginException;

import lombok.extern.slf4j.Slf4j;

/**
 * MustacheHelper 类是一个工具类，提供了处理 Mustache 模板的各种辅助方法。
 */
@Slf4j
public final class MustacheHelper {

    private MustacheHelper() {
    }

    private static final char SPECIAL_CHAR_4_PREPARED_STATEMENT = 16;
    private static final String SPECIAL_STRING_4_PREPARED_STATEMENT = SPECIAL_CHAR_4_PREPARED_STATEMENT + "";

    // MySQL IN 操作符的正则模式：xxx in (?)
    private static final Pattern MYSQL_IN_OPERATOR_PATTERN =
            Pattern.compile(".*\\s+(in|IN|In|iN)\\s*\\(\\s*" + SPECIAL_CHAR_4_PREPARED_STATEMENT + "\\s*\\)$");

    // Mustache 键的缓存
    private static final Cache<String, List<String>> MUSTACHE_KEY_CACHE;
    private static final long MUSTACHE_KEY_CACHE_MAX_SIZE = 100000;
    private static final int MUSTACHE_KEY_CACHE_EXPIRE_MINUTES = 15;

    // 静态代码块，初始化 Mustache 键的缓存
    static {
        MUSTACHE_KEY_CACHE = Caffeine.newBuilder()
                .maximumSize(MUSTACHE_KEY_CACHE_MAX_SIZE)
                .expireAfterWrite(Duration.ofMinutes(MUSTACHE_KEY_CACHE_EXPIRE_MINUTES))
                .build();
    }

    /**
     * 将 Mustache 模板字符串分解为普通文本和 Mustache 插值的列表。
     *
     * @param template Mustache 模板字符串，从中提取普通文本和插值标记。
     * @return 字符串标记的列表，这些标记构成给定模板字符串的一部分。连接此列表中的字符串应返回原始模板。标记被分割，使列表中的交替字符串为普通文本，其他为 Mustache 插值。
     */
    public static List<String> tokenize(String template) {
        List<String> strings = MUSTACHE_KEY_CACHE.get(template, MustacheHelper::doTokenize);
        return new ArrayList<>(strings);
    }

    /**
     * 将 Mustache 模板字符串分解为普通文本和 Mustache 插值的列表。
     *
     * @param template Mustache 模板字符串，从中提取普通文本和插值标记。
     * @return 字符串标记的列表，这些标记构成给定模板字符串的一部分。连接此列表中的字符串应返回原始模板。标记被分割，使列表中的交替字符串为普通文本，其他为 Mustache 插值。
     */
    private static List<String> doTokenize(String template) {
    // 如果模板为空，返回空列表
        if (isBlank(template)) {
            return Collections.emptyList();
        }

        List<String> tokens = new ArrayList<>();

        int length = template.length();

        // 以下是解析器的状态变量
        // isInsideMustache 指示指针是否在 mustache 双大括号内。为 true 时在其中，为 false 时不在。
        boolean isInsideMustache = false;

        // quote 指示指针在 JS 字符串中的引号字符。为 null 时表示不在任何 JS 字符串中。
        // 仅可以是 null、双引号 (")、单引号 (') 或反引号 (`) 之一。
        Character quote = null;

        // 在 mustache JS 内，此为当前 open/close 大括号的深度。
        int braceDepth = 0;

        StringBuilder currentToken = new StringBuilder().append(template.charAt(0));

        // 解析器实现为一个指针 (由 `i` 标记)，循环遍历模板字符串中的每个字符。
        // 解析器有两种主要状态：纯文本模式和 mustache 模式，当前状态由 `isInsideMustache` 指示。
        // 在指针遇到纯文本模式中的 `{{` 时，将其设置为 `true`；在指针遇到 mustache 模式中的 `}}` 时，
        // 但不在引号内时，将其设置为 `false`；在 mustache 模式中的引号内，`isInsideMustache` 状态不受影响。
        for (int i = 1; i < length; ++i) {
            char currentChar = template.charAt(i);
            char prevChar = template.charAt(i - 1);
            if (!isInsideMustache) {
                // 纯文本。
                if (currentChar == '{' && prevChar == '{') {
                    isInsideMustache = true;
                    // 删除添加到构建器中的 `{`
                    currentToken.deleteCharAt(currentToken.length() - 1);
                    clearAndPushToken(currentToken, tokens);
                    currentToken.append(prevChar);
                    braceDepth = 2;
                }
                currentToken.append(currentChar);
            } else {
                // Javascript
                if (quote != null) {
                    // 我们在 Javascript 字符串中。
                    if (currentChar == quote) {
                        // 计算此引号前的反斜杠数并确定其是否被转义。
                        int j = i;
                        do {
                            --j;
                        } while (template.charAt(j) == '\\');
                        int backslashCount = i - j - 1;
                        if (backslashCount % 2 == 0) {
                            // 此引号字符未被转义，因此它结束了引号字符串。
                            quote = null;
                        }
                    }
                    currentToken.append(currentChar);
                } else if (currentChar == '"' || currentChar == '\'' || currentChar == '`') {
                    // 此字符开始一个 Javascript 字符串。
                    quote = currentChar;
                    currentToken.append(currentChar);
                } else if (currentChar == '{') {
                    ++braceDepth;
                    currentToken.append(currentChar);
                } else if (currentChar == '}') {
                    --braceDepth;
                    currentToken.append(currentChar);
                    if (prevChar == '}' && braceDepth <= 0) {
                        clearAndPushToken(currentToken, tokens);
                        isInsideMustache = false;
                    }
                } else {
                    currentToken.append(currentChar);
                }
            }
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
        return tokens;
    }

    /**
     * 检查给定的 token 是否是 Mustache 模板的 token。
     *
     * @param token 要检查的 token
     * @return 如果 token 以 "{{" 开头且以 "}}" 结尾，返回 true，否则返回 false
     */
    public static boolean isMustacheToken(String token) {
        return token.startsWith("{{") && token.endsWith("}}");
    }

    /**
     * 对给定的 Mustache 模板字符串进行标记化，提取出 Mustache 插值，去掉前导和尾部的双大括号，
     * 去掉首尾空格，然后返回一组作为替换键的字符串集合。
     *
     * @param template Mustache 输入模板字符串。
     * @return 包含替换键的 Set 集合，其中前导和尾部的双大括号已去掉，并已去掉首尾空格。
     */
    public static Set<String> extractMustacheKeys(String template) {
        Set<String> keys = new HashSet<>();

        for (String token : tokenize(template)) {
            if (token.startsWith("{{") && token.endsWith("}}")) {
                // 允许添加空的 token，以便与之前的 `extractMustacheKeys` 方法兼容。
                // 在添加之前调用 `.trim()`，因为 Mustache 编译器在查找值之前会在模板中去掉键。
                keys.add(removeCurlyBraces(token));
            }
        }

        return keys;
    }

    /**
     * 从给定的 Mustache 模板字符串中提取出 Mustache 键，包括前导和尾部的双大括号。
     *
     * @param template Mustache 模板字符串
     * @return 包含 Mustache 键的 Set 集合
     */
    public static Set<String> extractMustacheKeysWithCurlyBraces(String template) {

        Set<String> keys = new HashSet<>();
        for (String token : tokenize(template)) {
            if (token.startsWith("{{") && token.endsWith("}}")) {
                // 允许添加空的 token，以便与之前的 `extractMustacheKeys` 方法兼容。
                // 调用 `.trim()` 之前添加，因为 Mustache 编译器在查找值之前会在模板中去掉键。
                keys.add(token);
            }
        }

        return keys;
    }

    /**
     * 去掉字符串中的大括号并返回剩余部分。
     *
     * @param token 要处理的字符串
     * @return 去掉大括号后的字符串
     */
    public static String removeCurlyBraces(String token) {
        // 去掉字符串的前2个字符和最后2个字符，然后去掉首尾空格
        return token.substring(2, token.length() - 2).trim();
    }

    /**
     * 从 Mustache 模板中提取键，并按在模板中出现的顺序返回键列表。
     * 包括重复的键。
     *
     * @param template 要处理的 Mustache 模板
     * @return 按在模板中出现的顺序排列的键列表
     */
    public static List<String> extractMustacheKeysInOrder(String template) {
        List<String> keys = new ArrayList<>();

        // 迭代模板中的标记
        for (String token : tokenize(template)) {
            // 如果标记以 "{{" 开头且以 "}}" 结尾
            if (token.startsWith("{{") && token.endsWith("}}")) {
                // 允许添加空标记以与以前的 `extractMustacheKeys` 方法兼容
                // 在添加之前调用 `.trim()` 以便与 Mustache 编译器在查找值之前在模板中剥离键
                keys.add(removeCurlyBraces(token));
            }
        }

        return keys;
    }

    /**
     * 清空 StringBuilder 并将其内容添加到列表中。
     *
     * @param tokenBuilder 要清空和添加的 StringBuilder
     * @param tokenList    要添加的列表
     */
    private static void clearAndPushToken(StringBuilder tokenBuilder, List<String> tokenList) {
        // 如果 StringBuilder 包含任何字符
        if (tokenBuilder.length() > 0) {
            // 将 StringBuilder 的内容添加到列表中
            tokenList.add(tokenBuilder.toString());
            // 清空 StringBuilder
            tokenBuilder.setLength(0);
        }
    }

    /**
     * 渲染 Mustache 模板并返回 JSON 数组字符串。
     *
     * @param jsonStr   要渲染的 JSON 字符串
     * @param paramMap  要应用于 Mustache 模板的键值对
     * @return 渲染后的 JSON 数组字符串
     */
    public static String renderMustacheArrayJsonString(String jsonStr, Map<String, Object> paramMap) {
        if (isBlank(jsonStr)) {
            return "[]";
        }
        return RjsonMustacheParser.renderMustacheJsonString(jsonStr, paramMap);
    }

    /**
     * 渲染 Mustache 模板并返回 JSON 字符串。
     *
     * @param jsonStr   要渲染的 JSON 字符串
     * @param paramMap  要应用于 Mustache 模板的键值对
     * @return 渲染后的 JSON 字符串
     */
    public static String renderMustacheJsonString(String jsonStr, Map<String, Object> paramMap) {
        return RjsonMustacheParser.renderMustacheJsonString(jsonStr, paramMap);
    }

    /**
     * 渲染 Mustache 模板并返回 JSON 节点。
     *
     * @param jsonStr   要渲染的 JSON 字符串
     * @param paramMap  要应用于 Mustache 模板的键值对
     * @return 渲染后的 JSON 节点
     */
    public static JsonNode renderMustacheJson(String jsonStr, Map<String, ?> paramMap) {
        return RjsonMustacheParser.renderMustacheJson(jsonStr, paramMap);
    }

    /**
     * 渲染 Mustache 模板并返回字符串。
     *
     * @param template  要渲染的 Mustache 模板
     * @param paramMap  要应用于 Mustache 模板的键值对
     * @return 渲染后的字符串
     */
    public static String renderMustacheString(String template, Map<String, ?> paramMap) {
        if (isBlank(template)) {
            return template;
        }

        List<String> tokenize = tokenize(template);
        return renderMustacheTokens(tokenize, paramMap);
    }

    /**
     * 渲染 Mustache 模板并返回字符串，不删除周围的括号。
     *
     * @param template  要渲染的 Mustache 模板
     * @param paramMap  要应用于 Mustache 模板的键值对
     * @return 渲染后的字符串
     */
    @SuppressWarnings("DuplicatedCode")
    public static String renderMustacheStringWithoutRemoveSurroundedPar(String template, Map<String, ?> paramMap) {
        if (isBlank(template)) {
            return template;
        }
        List<String> tokenize = tokenize(template);
        return renderMustacheTokens(tokenize, paramMap, false);
    }

    /**
     * 渲染 Mustache 模板并返回字符串数组。
     *
     * @param template  要渲染的 Mustache 模板数组
     * @param paramMap  要应用于 Mustache 模板的键值对
     * @return 渲染后的字符串数组
     */
    public static String[] renderMustacheArrayString(String[] template, Map<String, ?> paramMap) {
        return Arrays.stream(template)
                .map(s -> renderMustacheString(s, paramMap))
                .toArray(String[]::new);
    }

    /**
     * 渲染 Mustache 标记并返回字符串。
     *
     * @param tokens     要渲染的 Mustache 标记列表
     * @param paramMap   要应用于 Mustache 模板的键值对
     * @return 渲染后的字符串
     */
    public static String renderMustacheTokens(List<String> tokens, Map<String, ?> paramMap) {
        return renderMustacheTokens(tokens, paramMap, true);
    }

    /**
     * 渲染 Mustache 标记并返回字符串。
     *
     * @param tokens           要渲染的 Mustache 标记列表
     * @param paramMap         要应用于 Mustache 模板的键值对
     * @param removeSurroundedPar 是否删除周围的括号
     * @return 渲染后的字符串
     */
    @VisibleForTesting
    public static String renderMustacheTokens(List<String> tokens, Map<String, ?> paramMap, boolean removeSurroundedPar) {
        StringBuilder rendered = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.startsWith("{{") && token.endsWith("}}")) {
                Object mustacheValue = paramMap.get(token.substring(2, token.length() - 2).trim());
                String mustacheStrValue = convertToStringValue(mustacheValue);
                boolean isSurroundedByPar = isSurroundedByPar(tokens, i);
                if (removeSurroundedPar && isSurroundedByPar) {
                    removePreviousPar(rendered);
                    rendered.append(mustacheStrValue);
                    removeNextPar(tokens, i + 1);
                } else {
                    rendered.append(firstNonNull(mustacheStrValue, token)); // append original token is value is not found
                }
            } else {
                rendered.append(token);
            }
        }

        return StringEscapeUtils.unescapeHtml4(rendered.toString());
    }


        /**
     * 包含将Mustache模板字符串替换为问号并处理SQL中包含多个问号的情况的静态方法。
     */
    public static String replaceMustacheWithQuestionMarkMore(String query, List<String> mustacheBindings, Map<String, Object> param) {
        // 构建一个Map来存储Mustache绑定和特殊字符串的对应关系
        Map<String, String> replaceParamsMap =
                mustacheBindings.stream().collect(Collectors.toMap(Function.identity(), v -> SPECIAL_STRING_4_PREPARED_STATEMENT, (a, b) -> b));

        // 使用Mustache绑定渲染SQL查询字符串
        query = renderMustacheString(query, replaceParamsMap);

        // 处理SQL查询字符串中包含在引号内的多个问号
        String s = processMoreThanQuestionsInsideQuote(query, mustacheBindings, param, 0);

        // 将特殊字符替换为问号
        return s.replace(SPECIAL_CHAR_4_PREPARED_STATEMENT, '?');
    }

    /**
     * 包含将Mustache模板字符串替换为问号并处理SQL中包含多个问号的情况的静态方法。
     * 该方法还会处理SQL中的IN操作符。
     */
    public static String doPrepareStatement(String sql, List<String> mustacheKeys, Map<String, Object> param) {
        // 构建一个Map来存储Mustache绑定和特殊字符串的对应关系
        Map<String, String> replaceParamsMap = collectMap(mustacheKeys, Function.identity(), v -> SPECIAL_STRING_4_PREPARED_STATEMENT);

        // 使用Mustache绑定渲染SQL查询字符串
        sql = renderMustacheString(sql, replaceParamsMap);

        // 处理SQL查询字符串中包含在引号内的多个问号
        sql = processMoreThanQuestionsInsideQuote(sql, mustacheKeys, param, 0);

        try {
            // 处理SQL中的IN操作符
            sql = replaceParamWithInOperator(sql, mustacheKeys, param);
        } catch (Exception e) {
            // 若在处理IN操作符时发生异常，则抛出插件异常
            throw new PluginException(SQL_IN_OPERATOR_PARSE_ERROR, "SQL_IN_OPERATOR_PARSE_ERROR", e.getMessage());
        }

        // 将特殊字符替换为问号
        return sql.replace(SPECIAL_CHAR_4_PREPARED_STATEMENT, '?');
    }

    /**
     * 私有方法，用于将SQL中的IN操作符参数替换为问号。
     * 该方法会处理SQL中的IN操作符，并将其中的参数替换为问号。
     */
    private static String replaceParamWithInOperator(String sql, List<String> mustacheKeys, Map<String, Object> param) {
        // 如果Mustache绑定列表为空，则直接返回SQL
        if (CollectionUtils.isEmpty(mustacheKeys)) {
            return sql;
        }

        // 获取SQL中Mustache绑定出现的位置
        List<Integer> mustachePositions = getMustachePositions(sql);

        int startPos = 0;
        List<Integer> toRemoveParamIndexes = newArrayList();
        StringBuilder sqlStringBuilder = new StringBuilder();
        for (int i = 0; i < mustachePositions.size(); i++) {
            Object o = param.get(mustacheKeys.get(i));
            int currentPos = mustachePositions.get(i);

            // 若参数不是集合类型，则跳过该参数
            if (!(o instanceof Collection<?>)) {
                sqlStringBuilder.append(substring(sql, startPos, currentPos + 1));
                startPos = currentPos + 1;
                continue;
            }

            // 获取当前参数在SQL中的右括号位置
            int rightParIndex = findRightParIndex(sql, currentPos + 1);
            if (rightParIndex == -1) { // 若右括号未找到，则跳过该参数
                sqlStringBuilder.append(substring(sql, startPos, currentPos + 1));
                startPos = currentPos + 1;
                continue;
            }

            // 获取当前参数在SQL中的子串
            String substring = substring(sql, startPos, rightParIndex + 1);
            if (!matchInOperatorPattern(substring)) { // 若子串不符合"xxx in (?)"格式，则跳过该参数
                sqlStringBuilder.append(substring(sql, startPos, currentPos + 1));
                startPos = currentPos + 1;
                continue;
            }

            // 将参数转换为JSON数组，并将其中的"[]"去掉，然后将其替换为SQL中的参数
            String jsonArray = toJson(o);
            sqlStringBuilder.append(substring(sql, startPos, currentPos))
                    .append(substring(jsonArray, 1, jsonArray.length() - 1))
                    .append(") ");
            startPos = rightParIndex + 1;
            toRemoveParamIndexes.add(i);
        }

        // 将剩余的SQL字符串添加到sqlStringBuilder中
        sqlStringBuilder.append(substring(sql, startPos, sql.length()));
        // 从mustacheKeys中移除已被替换的参数
        removeReplacedKeys(mustacheKeys, toRemoveParamIndexes);
        return sqlStringBuilder.toString();
    }

    /**
     * 私有方法，用于从mustacheKeys中移除已被替换的参数。
     * 该方法会根据toRemoveParamIndexes中的索引来移除mustacheKeys中的参数。
     */
    private static void removeReplacedKeys(List<String> mustacheKeys, List<Integer> toRemoveParamIndexes) {
        toRemoveParamIndexes.stream()
                .sorted(Comparator.reverseOrder())
                .forEach(index -> mustacheKeys.remove((int) index));
    }

    /**
     * 私有方法，用于检查子串是否符合"xxx in (?)"格式。
     * 该方法会使用正则表达式来检查子串是否符合"xxx in (?)"格式。
     */
    private static boolean matchInOperatorPattern(String substring) {
        return MYSQL_IN_OPERATOR_PATTERN.matcher(substring).find();
    }

    /**
     * 私有方法，用于获取SQL中Mustache绑定出现的位置。
     * 该方法会返回一个包含Mustache绑定位置的列表。
     */
    @Nonnull
    private static List<Integer> getMustachePositions(String s) {
        List<Integer> mustachePositions = newArrayList();
        int startIndex = 0;
        while (true) {
            int index = s.indexOf(SPECIAL_CHAR_4_PREPARED_STATEMENT, startIndex);
            if (index == -1) {
                break;
            }
            mustachePositions.add(index);
            startIndex = index + 1;
        }
        return mustachePositions;
    }

        /**
     * 私有方法，用于在指定位置寻找SQL字符串中的右括号。
     * 该方法会从指定位置开始，逐个字符检查，直到找到右括号或到达字符串结尾。
     * 若找到右括号，则返回其在字符串中的位置。
     * 若未找到右括号，则返回-1。
     *
     * @param str 要检查的SQL字符串
     * @param index 开始检查的位置
     * @return 右括号在字符串中的位置，若未找到则返回-1
     */
    private static int findRightParIndex(String str, int index) {
        for (int j = index; j < str.length(); j++) {
            char ch = str.charAt(j);

            // 跳过空白字符
            if (Character.isWhitespace(ch)) {
                continue;
            }

            // 若找到右括号，则返回其位置
            if (ch == ')') {
                return j;
            }

            // 若未找到右括号，则返回-1
            return -1;
        }

        // 若未找到右括号，则返回-1
        return -1;
    }


    /**
     * 处理SQL查询字符串中包含在引号内的多个问号的静态方法。
     * 该方法会将查询字符串中包含在引号内的多个问号视为一个参数。
     * 例如：
     * select * from user where name like '?%';                        => select * from user where name like ?;
     * select * from user where name like '%?%?%';                     => select * from user where name like ?;
     * select * from user where name like '"?%"';                      => select * from user where name like ?;
     * select * from user where name like '?%' and name like '%?';     => select * from user where name like ? and name like ?;
     *
     * @param query 要处理的SQL查询字符串
     * @param bindingKeys 绑定的键列表
     * @param params 参数映射
     * @param generateKeyNumber 用于生成新键的数字
     * @return 处理后的SQL查询字符串
     */
    private static String processMoreThanQuestionsInsideQuote(String query, List<String> bindingKeys, Map<String, Object> params,
            int generateKeyNumber) {

        Map<Integer, Integer> questionPositionIndexMap = getQuestionPositionIndexMap(query);

        // 找到包含问号的引号对
        Range<Integer> range = findQuotePairWithQuestionInside(query);
        if (range.getMinimum() == -1) {
            return query;
        }

        // 获取问号的索引
        List<Integer> questionIndexes = new ArrayList<>();
        for (int i = range.getMinimum(); i < range.getMaximum(); i++) {
            if (query.charAt(i) == SPECIAL_CHAR_4_PREPARED_STATEMENT) {
                questionIndexes.add(questionPositionIndexMap.get(i));
            }
        }
        String quoteString = query.substring(range.getMinimum(), range.getMaximum());
        // 将所有问号合并成一个问号
        query = query.substring(0, range.getMinimum()) + SPECIAL_CHAR_4_PREPARED_STATEMENT + query.substring(range.getMaximum());
        String value = generateNewValue(bindingKeys, params, questionIndexes, quoteString);

        // 为新问号生成一个新的绑定键
        String key = "generateKey_" + generateKeyNumber;
        params.put(key, value);
        updateBindingKeys(bindingKeys, questionIndexes, key);

        return processMoreThanQuestionsInsideQuote(query, bindingKeys, params, generateKeyNumber + 1);
    }

        /**
     * 私有方法，用于更新绑定的键列表。
     * 该方法会将新键添加到绑定的键列表中，并将原始键从列表中移除。
     *
     * @param bindingKeys 绑定的键列表
     * @param questionIndexes 问号在绑定的键列表中的索引列表
     * @param key 要添加的新键
     */
    private static void updateBindingKeys(List<String> bindingKeys, List<Integer> questionIndexes, String key) {
        Integer lastQuestionIndex = Iterables.getLast(questionIndexes);
        bindingKeys.add(lastQuestionIndex + 1, key);

        // 按索引反向移除原始绑定键
        Collections.reverse(questionIndexes);
        for (int questionIndex : questionIndexes) {
            bindingKeys.remove(questionIndex);
        }
    }


    /**
     * 私有方法，用于生成新的值。
     * 该方法会将问号的值替换为新值。
     *
     * @param bindingKeys 绑定的键列表
     * @param params 参数映射
     * @param questionIndexes 问号在绑定的键列表中的索引列表
     * @param quoteString 包含问号的引号对
     * @return 新值
     */
    @NotNull
    private static String generateNewValue(List<String> bindingKeys, Map<String, Object> params, List<Integer> questionIndexes, String quoteString) {
        // 去掉引号
        String value = quoteString.substring(1, quoteString.length() - 1);
        // 按索引替换问号的值
        for (int questionIndex : questionIndexes) {
            Object valueObj = params.get(bindingKeys.get(questionIndex));
            if (valueObj == null) {
                value = value.replaceFirst(SPECIAL_STRING_4_PREPARED_STATEMENT, "");
                continue;
            }
            if (valueObj instanceof Number || valueObj instanceof Boolean || valueObj instanceof String) {
                value = value.replaceFirst(SPECIAL_STRING_4_PREPARED_STATEMENT, valueObj.toString());
                continue;
            }
            value = value.replaceFirst(SPECIAL_STRING_4_PREPARED_STATEMENT, JsonUtils.toJson(valueObj));
        }
        return value;
    }

    /**
     * 私有方法，用于获取问号在查询字符串中的位置索引映射。
     * 该方法会返回一个映射，其中键为查询字符串中的字符索引，值为问号在绑定的键列表中的索引。
     *
     * @param query 查询字符串
     * @return 位置索引映射
     */
    @NotNull
    private static Map<Integer, Integer> getQuestionPositionIndexMap(String query) {
        // 键：查询字符串中的字符索引，值：绑定的键列表中的问号索引
        Map<Integer, Integer> questionPositionIndexMap = new HashMap<>();
        int count = 0;
        for (int i = 0; i < query.length(); i++) {
            if (query.charAt(i) == SPECIAL_CHAR_4_PREPARED_STATEMENT) {
                questionPositionIndexMap.put(i, count++);
            }
        }
        return questionPositionIndexMap;
    }

    /**
     * 私有方法，用于在查询字符串中找到包含问号的引号对。
     * 该方法会返回一个范围，其中包含引号对的起始和结束索引。
     *
     * @param query 查询字符串
     * @return 引号对的范围
     */
    private static Range<Integer> findQuotePairWithQuestionInside(String query) {
        int startIndex = 0;
        while (true) {
            Range<Integer> range = findQuotePair(query, startIndex);
            if (range.getMinimum() == -1) {
                return range;
            }
            if (query.substring(range.getMinimum(), range.getMaximum()).contains(SPECIAL_STRING_4_PREPARED_STATEMENT)) {
                return range;
            }
            startIndex = range.getMaximum() + 1;
        }
    }

        /**
     * 私有方法，用于在查询字符串中找到包含在引号内的字符对。
     * 该方法会返回一个范围，其中包含引号对的起始和结束索引。
     *
     * @param query 查询字符串
     * @param startIndex 开始搜索的索引
     * @return 引号对的范围
     */
    private static Range<Integer> findQuotePair(String query, int startIndex) {
        char quote = 0; // 未找到引号
        int start = -1;
        for (int i = startIndex; i < query.length(); i++) {
            if (!isQuote(query, i)) {
                continue;
            }

            if (quote == 0) {
                quote = query.charAt(i);
                start = i;
            } else {
                if (query.charAt(i) == quote) {
                    return Range.between(start, i + 1);
                }
            }
        }
        return Range.between(-1, -1);
    }

    /**
     * 私有方法，用于检查字符是否为引号。
     * 该方法会检查字符是否为单引号或双引号，并且不在转义字符后面。
     *
     * @param query 查询字符串
     * @param index 要检查的字符的索引
     * @return true if the character is a quote, false otherwise
     */
    private static boolean isQuote(String query, int index) {
        char c = query.charAt(index);
        return (c == '\'' || c == '"') && !isEscape(query, index);
    }

    /**
     * 私有方法，用于检查字符是否在转义字符后面。
     * 该方法会检查字符是否在转义字符后面，转义字符为反斜杠。
     *
     * @param query 查询字符串
     * @param index 要检查的字符的索引
     * @return true if the character is escaped, false otherwise
     */
    private static boolean isEscape(String query, int index) {
        int count = 0;
        for (int i = index - 1; i >= 0; i--) {
            if (query.charAt(i) != '\\') {
                break;
            }
            count++;
        }
        // 找到奇数个反斜杠时，字符被转义
        return count % 2 == 1;
    }

    /**
     * 私有方法，用于将对象转换为字符串值。
     * 该方法会将对象转换为字符串值，如果对象为null，则返回空字符串。
     * 如果对象为集合或映射，则将其转换为JSON字符串。
     *
     * @param mustacheValue 要转换的对象
     * @return 转换后的字符串值
     */
    private static String convertToStringValue(Object mustacheValue) {
        if (mustacheValue == null) {
            return "";
        }

        if (mustacheValue instanceof Collection<?> || mustacheValue instanceof Map<?, ?>) {
            return toJson(mustacheValue);
        }

        return String.valueOf(mustacheValue);
    }

    /**
     * 私有方法，用于从令牌列表中删除下一个参数。
     * 该方法会将下一个参数的引号去掉。
     *
     * @param tokenize 令牌列表
     * @param index 要删除的下一个参数的索引
     */
    private static void removeNextPar(List<String> tokenize, int index) {
        tokenize.set(index, tokenize.get(index).substring(1));
    }

    /**
     * 私有方法，用于从渲染的字符串中删除上一个参数。
     * 该方法会将上一个参数的引号去掉。
     *
     * @param rendered 渲染的字符串
     */
    private static void removePreviousPar(StringBuilder rendered) {
        rendered.deleteCharAt(rendered.length() - 1);
    }

    /**
     * 私有方法，用于检查参数是否被引号包围。
     * 该方法会检查参数是否被引号包围，并且前一个参数以引号结束，后一个参数以引号开始。
     *
     * @param tokenize 令牌列表
     * @param index 要检查的参数的索引
     * @return true if the parameter is surrounded by quotes, false otherwise
     */
    private static boolean isSurroundedByPar(List<String> tokenize, int index) {
        if (index <= 0 || index >= tokenize.size() - 1) {
            return false;
        }

        return tokenize.get(index - 1).endsWith("\"") && tokenize.get(index + 1).startsWith("\"");
    }

}

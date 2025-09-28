package com.barda.sdk.util;

import javax.annotation.Nonnull;

import tv.twelvetone.json.JsonValue;
import tv.twelvetone.rjson.RJsonParser;
import tv.twelvetone.rjson.RJsonParserFactory;

/**
 * RjsonParser 类提供 JSON 字符串的解析功能。
 * 该类使用 RJsonParserFactory 类来创建 RJsonParser 实例，并使用 RJsonParser 实例来解析 JSON 字符串。
 */
public class RjsonParser {

    /**
     * RJsonParserFactory 实例，用于创建 RJsonParser 实例。
     */
    private static final RJsonParserFactory RJSON_PARSER_FACTORY = new RJsonParserFactory();

    /**
     * 解析 JSON 字符串并返回 JSON 值。
     *
     * @param jsonStr 要解析的 JSON 字符串
     * @return 解析后的 JSON 值
     * @throws Throwable 如果解析 JSON 字符串时发生错误
     */
    public static JsonValue parse(String jsonStr) throws Throwable {
        return getJsonParser().stringToValue(jsonStr);
    }

    /**
     * 获取 RJsonParser 实例。
     *
     * @return RJsonParser 实例
     */
    @Nonnull
    private static RJsonParser getJsonParser() {
        return RJSON_PARSER_FACTORY.createParser();
    }
}

package com.barda.sdk.util;

import static com.barda.sdk.util.JsonUtils.jsonNodeToObject;
import static com.barda.sdk.util.JsonUtils.toJson;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

/**
 * SqlGuiUtils 类提供 SQL GUI 相关的实用工具。
 * 该类包含了渲染 PostgreSQL 绑定值的功能，并提供了将值转换为 SQL 字符串的功能。
 */
public final class SqlGuiUtils {

    /**
     * 用于在 PostgreSQL 中转义 SQL 字符串的 EscapeSql 实现。
     */
    public static final EscapeSql POSTGRES_SQL_STR_ESCAPE = s -> {
        String randomTag = RandomStringUtils.randomAlphabetic(7);
        return "$" + randomTag + "$" + s + "$" + randomTag + "$";
    };

    /**
     * 私有构造函数，防止类被实例化。
     */
    private SqlGuiUtils() {
    }

    /**
     * 渲染 PostgreSQL 绑定值并返回 GUI SQL 值。
     *
     * @param obj 要渲染的对象
     * @param paramMap 包含 Mustache 模板中使用的参数
     * @return 渲染后的 GUI SQL 值
     */
    @Nonnull
    public static GuiSqlValue renderPsBindValue(Object obj, Map<String, ?> paramMap) {
        if (obj == null) {
            return GuiSqlValue.from(null);
        }

        if (obj instanceof String str) {
            if (isBlank(str)) {
                return GuiSqlValue.from(str);
            }

            JsonNode jsonNode = MustacheHelper.renderMustacheJson(str, paramMap);
            return GuiSqlValue.from(jsonNodeToObject(jsonNode));
        }

        return GuiSqlValue.from(obj);

    }


    /**
     * 包含 GUI SQL 值的类。
     */
    public static class GuiSqlValue {
        private final Object rawValue;

        /**
         * 私有构造函数，防止类被实例化。
         *
         * @param rawValue 原始值
         */
        public GuiSqlValue(Object rawValue) {
            this.rawValue = rawValue;
        }

        /**
         * 创建 GUI SQL 值。
         *
         * @param o 原始值
         * @return GUI SQL 值
         */
        public static GuiSqlValue from(Object o) {
            return new GuiSqlValue(o);
        }

        /**
         * 从 JSON 节点创建 GUI SQL 值。
         *
         * @param jsonNode JSON 节点
         * @return GUI SQL 值
         */
        public static GuiSqlValue fromJsonNode(JsonNode jsonNode) {
            if (jsonNode == null) {
                return from(null);
            }
            return from(jsonNodeToObject(jsonNode));
        }

        public Object getValue() {
            if (rawValue == null) {
                return null;
            }
            if (rawValue instanceof Collection<?> || rawValue instanceof Map<?, ?> || rawValue.getClass().isArray()) {
                return toJson(rawValue);
            }
            return rawValue;
        }

        /**
         * 获取原始值。
         *
         * @return 原始值
         */
        public Object getRawValue() {
            return rawValue;
        }

        /**
         * 获取转义后的 SQL 字符串。
         *
         * @param escapeFunc 转义函数
         * @return 转义后的 SQL 字符串
         */
        public String getConcatSqlStr(EscapeSql escapeFunc) {

            if (rawValue == null || rawValue instanceof Boolean || rawValue instanceof Number) {
                return String.valueOf(rawValue);
            }

            if (rawValue instanceof String strValue) {
                return escapeFunc.escape(strValue);
            }

            if (rawValue instanceof Map<?, ?> || rawValue instanceof Collection<?>) {
                return escapeFunc.escape(toJson(rawValue));
            }

            return String.valueOf(rawValue);

        }

        /**
         * 用于在 SQL 字符串中转义值的接口。
         */
        public interface EscapeSql {

            /**
             * 转义 SQL 字符串。
             *
             * @param stringValue 要转义的 SQL 字符串
             * @return 转义后的 SQL 字符串
             */
            String escape(String stringValue);
        }
    }
}

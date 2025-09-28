package com.barda.sdk.plugin.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * QueryExecutionUtils提供了一组用于查询执行的实用方法。
 */
public class QueryExecutionUtils {

    // 共享的调度器
    private static final Scheduler SHARED_SCHEDULER = Schedulers.newBoundedElastic(100,
            10000
            , "plugin-executor");

    /**
     * 获取共享的调度器。
     *
     * @return 共享的调度器
     */
    public static Scheduler querySharedScheduler() {
        return SHARED_SCHEDULER;
    }

    /**
     * 获取重复的列名列表。
     *
     * @param columnNames 列名列表
     * @return 重复的列名列表
     */
    public static List<String> getIdenticalColumns(List<String> columnNames) {
        Map<String, Long> columnFrequencies = columnNames
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return columnFrequencies.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 在表单数据中验证是否存在有效的配置项。
     *
     * @param formData 表单数据
     * @param field    字段名
     * @return 若存在有效的配置项则返回true，否则返回false
     */
    public static Boolean validConfigurationPresentInFormData(Map<String, Object> formData, String field) {
        return getValueSafelyFromFormData(formData, field) != null;
    }

    /**
     * 从表单数据中安全地获取值，如果值不存在则返回默认值。
     *
     * @param formData    表单数据
     * @param field       字段名
     * @param type        值的类型
     * @param defaultValue 默认值
     * @param <T>         值的类型
     * @return 表单中指定字段的值或默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValueSafelyFromFormData(Map<String, Object> formData, String field, Class<T> type,
            T defaultValue) {
        Object formDataValue = getValueSafelyFromFormData(formData, field);
        return formDataValue != null ? (T) formDataValue : defaultValue;
    }

    /**
     * 获取表单数据中指定字段的值并转换为指定类型。
     * 如果字段的值为null或转换失败，返回null。
     *
     * @param <T> 要转换的类型
     * @param formData 表单数据
     * @param field 字段名称
     * @param type 要转换的类型
     * @return 转换后的类型值，如果转换失败或字段的值为null，返回null
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValueSafelyFromFormData(Map<String, Object> formData, String field, Class<T> type) {
        return (T) (getValueSafelyFromFormData(formData, field));
    }

    /**
     * 获取表单数据中指定字段的字符串值并安全地转换为String类型。
     * 如果字段的值为null或转换失败，返回null。
     *
     * @param formData 表单数据
     * @param field 字段名称
     * @return 转换后的String类型值，如果转换失败或字段的值为null，返回null
     */
    public static String getStringValueSafelyFromFormData(Map<String, Object> formData, String field) {
        return getValueSafelyFromFormData(formData, field, String.class);
    }

    // 其他方法的文档见代码注释中的中文注释
    /**
     * 获取表单数据中指定字段的值，支持嵌套的字段。
     * 如果表单数据为空、字段为空或字段的值为null，返回null。
     *
     * @param formData 表单数据
     * @param field 字段名称，可以包含嵌套的字段，例如 "parent.child"
     * @return 字段的值，如果字段的值为null或转换失败，返回null
     */
    @SuppressWarnings("unchecked")
    public static Object getValueSafelyFromFormData(Map<String, Object> formData, String field) {
        if (MapUtils.isEmpty(formData)) {
            return null;
        }

        if (!field.contains(".")) {
            return formData.getOrDefault(field, null);
        }

        String[] fieldNames = field.split("\\.");

        Map<String, Object> nestedMap = (Map<String, Object>) formData.get(fieldNames[0]);

        String[] trimmedFieldNames = Arrays.copyOfRange(fieldNames, 1, fieldNames.length);
        String nestedFieldName = String.join(".", trimmedFieldNames);

        return getValueSafelyFromFormData(nestedMap, nestedFieldName);

    }

    /**
     * 获取表单数据中指定字段的值并返回默认值。
     * 如果表单数据为空、字段为空或字段的值为null，返回默认值。
     *
     * @param formData 表单数据
     * @param field 字段名称
     * @param defaultValue 默认值
     * @return 字段的值，如果字段的值为null，返回默认值
     */
    public static Object getValueSafelyFromFormDataOrDefault(Map<String, Object> formData, String field, Object defaultValue) {
        Object value = getValueSafelyFromFormData(formData, field);

        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    // 其他方法的文档见代码注释中的中文注释
    /**
     * 安全地在表单数据中设置指定字段的值。
     * 如果表单数据为空，将创建一个新的HashMap。
     * 支持嵌套的字段。
     *
     * @param formData 表单数据
     * @param field 字段名称，可以包含嵌套的字段，例如 "parent.child"
     * @param value 要设置的值
     */
    @SuppressWarnings("unchecked")
    public static void setValueSafelyInFormData(Map<String, Object> formData, String field, Object value) {

        if (formData == null) {
            formData = new HashMap<>();
        }

        if (!field.contains(".")) {
            formData.put(field, value);
            return;
        }

        String[] fieldNames = field.split("\\.");

        formData.putIfAbsent(fieldNames[0], new HashMap<String, Object>());

        Map<String, Object> nestedMap = (Map<String, Object>) formData.get(fieldNames[0]);

        String[] trimmedFieldNames = Arrays.copyOfRange(fieldNames, 1, fieldNames.length);
        String nestedFieldName = String.join(".", trimmedFieldNames);

        setValueSafelyInFormData(nestedMap, nestedFieldName, value);
    }

}
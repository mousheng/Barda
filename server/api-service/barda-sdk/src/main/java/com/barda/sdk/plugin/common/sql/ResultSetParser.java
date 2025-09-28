package com.barda.sdk.plugin.common.sql;

import static com.barda.sdk.util.DateTimeUtils.DATE_TIME_FORMAT;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

/**
 * ResultSetParser用于解析SQL结果集。
 */
public class ResultSetParser {

    /**
     * 表示日期类型的列名
     */
    public static final String DATE_COLUMN_TYPE_NAME = "date";

    /**
     * 表示日期时间类型的列名
     */
    public static final String DATETIME_COLUMN_TYPE_NAME = "datetime";

    /**
     * 表示时间戳类型的列名
     */
    public static final String TIMESTAMP_COLUMN_TYPE_NAME = "timestamp";

    /**
     * 表示年份类型的列名
     */
    public static final String YEAR_COLUMN_TYPE_NAME = "year";

    /**
     * 解析ResultSet中的所有行，并返回一个包含行数据的Map列表。
     *
     * @param resultSet 要解析的ResultSet对象
     * @return 包含行数据的Map列表
     * @throws SQLException 如果解析过程中发生SQL异常
     */
    public static List<Map<String, Object>> parseRows(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> result = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = parseRow(resultSet, metaData, columnCount);
            result.add(row);
        }
        return result;
    }

    /**
     * 解析ResultSet中的一行数据。
     *
     * @param resultSet  要解析的ResultSet对象
     * @param metaData   ResultSetMetaData对象，用于获取列的元数据信息
     * @param colCount   结果集中的列数
     * @return 包含列名和对应值的Map对象
     * @throws SQLException 如果解析过程中发生SQL异常
     */
    private static Map<String, Object> parseRow(ResultSet resultSet, ResultSetMetaData metaData, int colCount) throws SQLException {
        Map<String, Object> row = new LinkedHashMap<>(colCount);

        // 第一列的索引是1，第二列是2，以此类推...
        for (int i = 1; i <= colCount; i++) {
            String typeName = metaData.getColumnTypeName(i);
            Object value = getValue(resultSet, i, typeName);
            row.put(metaData.getColumnLabel(i), value);
        }
        return row;
    }

    /**
     * 获取ResultSet中指定列的值。
     *
     * @param resultSet ResultSet对象
     * @param i         列索引
     * @param typeName  列类型名称
     * @return 列值
     * @throws SQLException 如果读取值时发生SQL异常
     */
    @Nullable
    private static Object getValue(ResultSet resultSet, int i, String typeName) throws SQLException {

        // 这个问题的特殊处理:
        // com.mysql.cj.exceptions.DataReadException: The value '30:00:00' is an invalid TIME value.
        // JDBC Time对象表示的是一个墙上的时钟时间，而不是MySQL所认为的一个持续时间。
        // 如果你将这个类型视为一个持续时间，请考虑将这个值作为字符串检索，并根据你的要求进行处理。
        if (typeName.equalsIgnoreCase("TIME") && isDuration(resultSet, i)) {
            return resultSet.getString(i);
        }

        if (resultSet.getObject(i) == null) {
            return null;
        }
        if (DATE_COLUMN_TYPE_NAME.equalsIgnoreCase(typeName)) {
            return DateTimeFormatter.ISO_DATE.format(resultSet.getDate(i).toLocalDate());
        }
        if (DATETIME_COLUMN_TYPE_NAME.equalsIgnoreCase(typeName)
                || TIMESTAMP_COLUMN_TYPE_NAME.equalsIgnoreCase(typeName)) {
            return DATE_TIME_FORMAT.format(LocalDateTime.of(resultSet.getDate(i).toLocalDate(), resultSet.getTime(i).toLocalTime()));
        }
        if (YEAR_COLUMN_TYPE_NAME.equalsIgnoreCase(typeName)) {
            return resultSet.getDate(i).toLocalDate().getYear();
        }
        return resultSet.getObject(i);
    }

    /**
     * 检查ResultSet中指定列是否表示持续时间。
     *
     * @param resultSet ResultSet对象
     * @param i         列索引
     * @return 如果列表示持续时间，则返回true；否则返回false
     */
    private static boolean isDuration(ResultSet resultSet, int i) {
        try {
            resultSet.getObject(i);
        } catch (SQLException e) {
            return e.getMessage().contains("JDBC Time objects represent a wall-clock");
        }
        return false;
    }


    /**
     * 解析ResultSetMetaData中的列名，并返回列名列表。
     *
     * @param metaData 要解析的ResultSetMetaData对象
     * @return 列名列表
     * @throws SQLException 如果解析过程中发生SQL异常
     */
    public static List<String> parseColumns(ResultSetMetaData metaData) throws SQLException {
        return IntStream
                .range(1, metaData.getColumnCount() + 1) // JDBC列索引从1开始
                .mapToObj(i -> {
                    try {
                        return metaData.getColumnLabel(i);
                    } catch (SQLException exception) {
                        throw new RuntimeException(exception);
                    }
                })
                .collect(Collectors.toList());
    }
}

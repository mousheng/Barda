package com.barda.plugin.clickhouse.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import com.barda.sdk.models.DatasourceStructure.Column;
import com.barda.sdk.models.DatasourceStructure.Table;
import com.barda.sdk.models.DatasourceStructure.TableType;

/**
 * ClickHouseStructureParser 类用于解析 ClickHouse 数据库的表和列结构。
 */
@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class ClickHouseStructureParser {

    /**
     * SQL 查询，用于获取表和列的元数据。
     */
    public static final String COLUMNS_QUERY = """
             select tab.table_name as table_name,
                               col.ordinal_position as column_id,
                               col.column_name as column_name,
                               col.data_type as column_type,
                               col.is_nullable != 0 as is_nullable
            from information_schema.tables as tab
                 inner join information_schema.columns as col
                            on col.table_schema = tab.table_schema
                                and col.table_name = tab.table_name
                                and col.table_schema = database()
            order by tab.table_name,
                     col.ordinal_position;
                     """;

    /**
     * 解析表和列的结构。
     *
     * @param tablesByName 表的名称与表的映射
     * @param statement 用于执行 SQL 查询的 Statement
     * @throws SQLException 如果执行 SQL 查询时发生错误
     */
    public static void parseTableAndColumns(Map<String, Table> tablesByName, Statement statement) throws SQLException {
        try (ResultSet columnsResultSet = statement.executeQuery(COLUMNS_QUERY)) {
            while (columnsResultSet.next()) {
                String tableName = columnsResultSet.getString("table_name");

                Table table = tablesByName.computeIfAbsent(tableName, __ -> new Table(
                        TableType.TABLE, "", tableName,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new ArrayList<>()
                ));

                table.addColumn(new Column(
                        columnsResultSet.getString("column_name"),
                        columnsResultSet.getString("column_type"),
                        null,
                        false
                ));
            }
        }
    }
}

package com.barda.sdk.plugin.common.sql;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.MapUtils.getString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.barda.sdk.models.DatasourceStructure.Column;
import com.barda.sdk.models.DatasourceStructure.Table;
import com.barda.sdk.models.DatasourceStructure.TableType;

/**
 * StructureParser用于解析数据库结构信息。
 */
public class StructureParser {

    /**
     * 查询数据库结构的SQL语句，Oracle会自动将结果转换为大写，使用[as "table_name"]可以保留其原有大小写。
     */
    public static final String QUERY_STRUCTURE_SQL = """
            SELECT
            	table_name as "table_name",
            	column_name as "column_name",
            	data_type as "data_type"
            FROM
            	user_tab_cols
            WHERE
                hidden_column = 'NO'
            """;

    /**
     * 解析ResultSet中的列信息，并返回一个包含Table对象的列表。
     *
     * @param resultSet 要解析的ResultSet对象
     * @return 包含Table对象的列表
     * @throws SQLException 如果解析过程中发生SQL异常
     */
    public static List<Table> parseColumns(ResultSet resultSet) throws SQLException {

        List<Map<String, Object>> rows = ResultSetParser.parseRows(resultSet);

        Map<String, Table> tables = new HashMap<>();
        for (Map<String, Object> columnMap : rows) {
            String tableName = getString(columnMap, "table_name");
            Table table = tables.computeIfAbsent(tableName,
                    k -> new Table(TableType.TABLE, null, k, new ArrayList<>(), emptyList(), emptyList()));

            String columnName = getString(columnMap, "column_name");
            String dataType = getString(columnMap, "data_type");
            table.addColumn(new Column(columnName, dataType, null, null));
        }
        return new ArrayList<>(tables.values());
    }
}

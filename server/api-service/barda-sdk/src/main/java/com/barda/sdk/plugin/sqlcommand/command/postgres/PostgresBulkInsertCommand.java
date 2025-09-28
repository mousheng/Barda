package com.barda.sdk.plugin.sqlcommand.command.postgres;

import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;
import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.POSTGRES_COLUMN_DELIMITER;
import static com.barda.sdk.util.SqlGuiUtils.POSTGRES_SQL_STR_ESCAPE;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.BulkInsertCommand;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

/**
 * 一个用于执行 PostgreSQL 批量插入的命令类。
 * 继承自 {@link BulkInsertCommand}，并实现了特定于 PostgreSQL 的功能。
 */
public class PostgresBulkInsertCommand extends BulkInsertCommand {

    /**
     * 构造函数。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含要插入的对象的集合
     */
    protected PostgresBulkInsertCommand(String table, BulkObjectChangeSet bulkObjectChangeSet) {
        super(table, bulkObjectChangeSet, POSTGRES_COLUMN_DELIMITER, POSTGRES_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息中创建一个 {@link PostgresBulkInsertCommand} 实例。
     *
     * @param commandDetail 命令详细信息
     * @return 创建的 {@link PostgresBulkInsertCommand} 实例
     */
    public static BulkInsertCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new PostgresBulkInsertCommand(table, bulkObjectChangeSet);
    }

    /**
     * 指示是否使用原始 SQL 渲染此命令。
     *
     * @return 如果使用原始 SQL 渲染，则返回 true，否则返回 false
     */
    @Override
    public boolean isRenderWithRawSql() {
        return true;
    }

    /**
     * 获取用于转义 SQL 字符串的函数。
     *
     * @return 转义 SQL 字符串的函数
     */
    @Override
    public EscapeSql escapeStrFunc() {
        return POSTGRES_SQL_STR_ESCAPE;
    }
}

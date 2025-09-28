package com.barda.sdk.plugin.sqlcommand.command.postgres;

import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;
import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parsePrimaryKey;
import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.POSTGRES_COLUMN_DELIMITER;
import static com.barda.sdk.util.SqlGuiUtils.POSTGRES_SQL_STR_ESCAPE;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.BulkUpdateCommand;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

/**
 * 一个用于执行 PostgreSQL 批量更新的命令类。
 * 继承自 {@link BulkUpdateCommand}，并实现了特定于 PostgreSQL 的功能。
 */
public class PostgresBulkUpdateCommand extends BulkUpdateCommand {

    /**
     * 构造函数。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含要更新的对象的集合
     * @param primaryKey 表的主键
     */
    protected PostgresBulkUpdateCommand(String table, BulkObjectChangeSet bulkObjectChangeSet,
            String primaryKey) {
        super(table, bulkObjectChangeSet, primaryKey, POSTGRES_COLUMN_DELIMITER, POSTGRES_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息中创建一个 {@link PostgresBulkUpdateCommand} 实例。
     *
     * @param commandDetail 命令详细信息
     * @return 创建的 {@link PostgresBulkUpdateCommand} 实例
     */
    public static PostgresBulkUpdateCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new PostgresBulkUpdateCommand(table, bulkObjectChangeSet, parsePrimaryKey(commandDetail));
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

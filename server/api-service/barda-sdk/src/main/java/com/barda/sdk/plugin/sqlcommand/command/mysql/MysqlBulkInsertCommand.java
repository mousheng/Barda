package com.barda.sdk.plugin.sqlcommand.command.mysql;

import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;
import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.MYSQL_COLUMN_DELIMITER;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.BulkInsertCommand;

/**
 * 一个用于执行 MySQL 批量插入的命令类。
 * 继承自 {@link BulkInsertCommand}，并使用 MySQL 特定的列分隔符。
 */
public class MysqlBulkInsertCommand extends BulkInsertCommand {

    /**
     * 构造函数。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含批量插入对象的 {@link BulkObjectChangeSet}
     */
    protected MysqlBulkInsertCommand(String table, BulkObjectChangeSet bulkObjectChangeSet) {
        super(table, bulkObjectChangeSet, MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息中创建一个 {@link MysqlBulkInsertCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return 一个新的 {@link MysqlBulkInsertCommand} 实例
     */
    public static BulkInsertCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new MysqlBulkInsertCommand(table, bulkObjectChangeSet);
    }
}

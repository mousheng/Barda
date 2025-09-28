package com.barda.sdk.plugin.sqlcommand.command.mysql;

import static com.barda.sdk.plugin.sqlcommand.GuiSqlCommand.parseTable;
import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;
import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parsePrimaryKey;
import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.MYSQL_COLUMN_DELIMITER;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.BulkUpdateCommand;

/**
 * 一个用于执行 MySQL 批量更新的命令类。
 * 继承自 {@link BulkUpdateCommand}，并使用 MySQL 特定的列分隔符。
 */
public class MysqlBulkUpdateCommand extends BulkUpdateCommand {

    /**
     * 构造函数。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含批量更新对象的 {@link BulkObjectChangeSet}
     * @param primaryKey 表的主键
     */
    protected MysqlBulkUpdateCommand(String table, BulkObjectChangeSet bulkObjectChangeSet,
            String primaryKey) {
        super(table, bulkObjectChangeSet, primaryKey, MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息中创建一个 {@link MysqlBulkUpdateCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return 一个新的 {@link MysqlBulkUpdateCommand} 实例
     */
    public static MysqlBulkUpdateCommand from(Map<String, Object> commandDetail) {
        String table = parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new MysqlBulkUpdateCommand(table, bulkObjectChangeSet, parsePrimaryKey(commandDetail));
    }
}
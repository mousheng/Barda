package com.barda.plugin.oracle.gui;

import static com.barda.plugin.oracle.gui.GuiConstants.COLUMN_DELIMITER_FRONT;
import static com.barda.sdk.plugin.sqlcommand.GuiSqlCommand.parseTable;
import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;
import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parsePrimaryKey;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.BulkUpdateCommand;

/**
 * OracleBulkUpdateCommand 类是 BulkUpdateCommand 的子类，
 * 用于执行 Oracle 数据库的批量更新操作。
 */
public class OracleBulkUpdateCommand extends BulkUpdateCommand {

    /**
     * 构造函数，初始化 OracleBulkUpdateCommand 实例。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含批量数据更改的集合
     * @param primaryKey 表的主键
     */
    protected OracleBulkUpdateCommand(String table, BulkObjectChangeSet bulkObjectChangeSet, String primaryKey) {
        super(table, bulkObjectChangeSet, primaryKey, COLUMN_DELIMITER_FRONT);
    }

    /**
     * 工厂方法，从 Map 类型的数据中创建 OracleBulkUpdateCommand 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return OracleBulkUpdateCommand 实例
     */
    public static OracleBulkUpdateCommand from(Map<String, Object> commandDetail) {
        String table = parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new OracleBulkUpdateCommand(table, bulkObjectChangeSet, parsePrimaryKey(commandDetail));
    }
}

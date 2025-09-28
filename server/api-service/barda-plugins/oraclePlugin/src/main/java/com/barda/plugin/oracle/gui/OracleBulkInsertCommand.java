package com.barda.plugin.oracle.gui;

import static com.barda.plugin.oracle.gui.GuiConstants.COLUMN_DELIMITER_FRONT;
import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.BulkInsertCommand;

/**
 * OracleBulkInsertCommand 类是 BulkInsertCommand 的子类，
 * 用于执行 Oracle 数据库的批量插入操作。
 */
public class OracleBulkInsertCommand extends BulkInsertCommand {

    /**
     * 构造函数，初始化 OracleBulkInsertCommand 实例。
     *
     * @param table 表名
     * @param bulkObjectChangeSet 包含批量数据更改的集合
     */
    protected OracleBulkInsertCommand(String table, BulkObjectChangeSet bulkObjectChangeSet) {
        super(table, bulkObjectChangeSet, COLUMN_DELIMITER_FRONT);
    }

    /**
     * 工厂方法，从 Map 类型的数据中创建 OracleBulkInsertCommand 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return OracleBulkInsertCommand 实例
     */
    public static BulkInsertCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new OracleBulkInsertCommand(table, bulkObjectChangeSet);
    }
}

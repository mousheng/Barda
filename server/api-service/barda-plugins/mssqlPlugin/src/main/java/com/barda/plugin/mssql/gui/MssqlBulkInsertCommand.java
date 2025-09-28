package com.barda.plugin.mssql.gui;

import static com.barda.plugin.mssql.gui.GuiConstants.MSSQL_COLUMN_DELIMITER_BACK;
import static com.barda.plugin.mssql.gui.GuiConstants.MSSQL_COLUMN_DELIMITER_FRONT;
import static com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.BulkInsertCommand;

public class MssqlBulkInsertCommand extends BulkInsertCommand {
    protected MssqlBulkInsertCommand(String table, BulkObjectChangeSet bulkObjectChangeSet) {
        super(table, bulkObjectChangeSet, MSSQL_COLUMN_DELIMITER_FRONT, MSSQL_COLUMN_DELIMITER_BACK);
    }

    public static BulkInsertCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new MssqlBulkInsertCommand(table, bulkObjectChangeSet);
    }
}

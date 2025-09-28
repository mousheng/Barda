package com.barda.sdk.plugin.sqlcommand.command.mysql;

import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.MYSQL_COLUMN_DELIMITER;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.InsertCommand;

/**
 * 一个用于执行 MySQL 插入操作的命令类。
 * 继承自 {@link InsertCommand}，并使用 MySQL 特定的列分隔符。
 */
public class MysqlInsertCommand extends InsertCommand {

    private MysqlInsertCommand(Map<String, Object> commandDetail) {
        super(commandDetail, MYSQL_COLUMN_DELIMITER);
    }

    /**
     * 私有构造函数，仅在测试中使用。
     *
     * @param table 表名
     * @param changeSet 包含插入操作的 {@link ChangeSet}
     */
    @VisibleForTesting
    protected MysqlInsertCommand(String table, ChangeSet changeSet) {
        super(table, changeSet, MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER);
    }

    /**
     * 从命令详情创建MySQL插入命令对象。
     *
     * @param commandDetail 命令详情
     * @return MySQL插入命令对象
     */
    public static MysqlInsertCommand from(Map<String, Object> commandDetail) {
        return new MysqlInsertCommand(commandDetail);
    }
}

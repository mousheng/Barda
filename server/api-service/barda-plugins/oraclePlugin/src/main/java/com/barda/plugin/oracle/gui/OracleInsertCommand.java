package com.barda.plugin.oracle.gui;

import static com.barda.plugin.oracle.gui.GuiConstants.COLUMN_DELIMITER_FRONT;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.InsertCommand;

/**
 * OracleInsertCommand 类是 InsertCommand 的子类，
 * 用于执行 Oracle 数据库的插入操作。
 */
public class OracleInsertCommand extends InsertCommand {

    /**
     * 私有构造函数，仅在本类中使用。
     *
     * @param commandDetail 插入命令的详细信息
     */
    private OracleInsertCommand(Map<String, Object> commandDetail) {
        super(commandDetail, COLUMN_DELIMITER_FRONT);
    }

    /**
     * 仅供测试使用的受保护的构造函数，
     * 用于在测试中创建 OracleInsertCommand 实例。
     *
     * @param table 表名
     * @param changeSet 包含更改的集合
     */
    @VisibleForTesting
    protected OracleInsertCommand(String table, ChangeSet changeSet) {
        super(table, changeSet, COLUMN_DELIMITER_FRONT, COLUMN_DELIMITER_FRONT);
    }

    /**
     * 工厂方法，从 Map 类型的数据中创建 OracleInsertCommand 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return OracleInsertCommand 实例
     */
    public static OracleInsertCommand from(Map<String, Object> commandDetail) {
        return new OracleInsertCommand(commandDetail);
    }
}

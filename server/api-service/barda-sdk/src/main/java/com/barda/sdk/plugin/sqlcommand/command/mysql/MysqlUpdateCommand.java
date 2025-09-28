package com.barda.sdk.plugin.sqlcommand.command.mysql;

import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.MYSQL_COLUMN_DELIMITER;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.UpdateCommand;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;

/**
 * 一个用于执行 MySQL 更新操作的命令类。
 * 继承自 {@link UpdateCommand}，并使用 MySQL 特定的列分隔符。
 */
public class MysqlUpdateCommand extends UpdateCommand {

    /**
     * 私有构造函数，仅在 {@link MysqlUpdateCommand#from(Map)} 中使用。
     *
     * @param commandDetail 包含命令详细信息的 Map
     */
    private MysqlUpdateCommand(Map<String, Object> commandDetail) {
        super(commandDetail, MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER);
    }

    /**
     * 私有构造函数，仅在测试中使用。
     *
     * @param table 表名
     * @param changeSet 包含更新操作的 {@link ChangeSet}
     * @param filterSet 包含更新操作的过滤器集合 {@link FilterSet}
     * @param allowMultiModify 指示是否允许执行多行修改
     */
    @VisibleForTesting
    protected MysqlUpdateCommand(String table, ChangeSet changeSet, FilterSet filterSet, boolean allowMultiModify) {
        super(table, changeSet, filterSet, allowMultiModify, MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息中创建一个 {@link MysqlUpdateCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return 一个新的 {@link MysqlUpdateCommand} 实例
     */
    public static MysqlUpdateCommand from(Map<String, Object> commandDetail) {
        return new MysqlUpdateCommand(commandDetail);
    }
}

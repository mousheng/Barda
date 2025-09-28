package com.barda.sdk.plugin.sqlcommand.command.mysql;

import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.MYSQL_COLUMN_DELIMITER;
import static com.barda.sdk.plugin.sqlcommand.filter.FilterSet.parseFilterSet;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.command.DeleteCommand;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;

/**
 * 一个用于执行 MySQL 删除操作的命令类。
 * 继承自 {@link DeleteCommand}，并使用 MySQL 特定的列分隔符。
 */
public class MysqlDeleteCommand extends DeleteCommand {

    /**
     * 构造函数。
     *
     * @param table 表名
     * @param filterSet 包含删除操作的过滤器集合 {@link FilterSet}
     * @param allowMultiModify 指示是否允许执行多行修改
     */
    protected MysqlDeleteCommand(String table, FilterSet filterSet, boolean allowMultiModify) {
        super(table, filterSet, allowMultiModify, MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息中创建一个 {@link MysqlDeleteCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return 一个新的 {@link MysqlDeleteCommand} 实例
     */
    public static DeleteCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        FilterSet filterSet = parseFilterSet(commandDetail);
        boolean allowMultiModify = GuiSqlCommand.parseAllowMultiModify(commandDetail);
        return new MysqlDeleteCommand(table, filterSet, allowMultiModify);
    }
}
package com.barda.sdk.plugin.sqlcommand.command.postgres;

import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.POSTGRES_COLUMN_DELIMITER;
import static com.barda.sdk.util.SqlGuiUtils.POSTGRES_SQL_STR_ESCAPE;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.InsertCommand;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

/**
 * 一个用于在 PostgreSQL 数据库中执行 INSERT 命令的类。
 * 它继承自 {@link InsertCommand} 并实现了特定于 PostgreSQL 的功能。
 */
public class PostgresInsertCommand extends InsertCommand {

    /**
     * 私有构造函数，用于从命令详细信息创建 {@link PostgresInsertCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     */
    private PostgresInsertCommand(Map<String, Object> commandDetail) {
        super(commandDetail, POSTGRES_COLUMN_DELIMITER);
    }

    /**
     * 仅用于测试的受保护的构造函数，用于从表名和 {@link ChangeSet} 创建 {@link PostgresInsertCommand} 实例。
     *
     * @param table 表名
     * @param changeSet 包含更改的 ChangeSet
     */
    @VisibleForTesting
    protected PostgresInsertCommand(String table, ChangeSet changeSet) {
        super(table, changeSet, POSTGRES_COLUMN_DELIMITER, POSTGRES_COLUMN_DELIMITER);
    }

    /**
     * 从命令详细信息创建一个 {@link PostgresInsertCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return 新的 {@link PostgresInsertCommand} 实例
     */
    public static PostgresInsertCommand from(Map<String, Object> commandDetail) {
        return new PostgresInsertCommand(commandDetail);
    }


    /**
     * {@inheritDoc}
     *
     * 重写此方法以返回 true，表示在执行 SQL 之前需要使用原始 SQL 渲染命令。
     */
    @Override
    public boolean isRenderWithRawSql() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * 重写此方法以返回 PostgreSQL 特定的转义 SQL 功能。
     */
    @Override
    public EscapeSql escapeStrFunc() {
        return POSTGRES_SQL_STR_ESCAPE;
    }
}

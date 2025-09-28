package com.barda.sdk.plugin.sqlcommand.command.mysql;

import static com.google.common.collect.Lists.newArrayList;
import static com.barda.sdk.exception.PluginCommonError.INVALID_UPSERT_COMMAND;
import static com.barda.sdk.plugin.common.constant.Constants.INSERT_CHANGE_SET_FORM_KEY;
import static com.barda.sdk.plugin.common.constant.Constants.UPDATE_CHANGE_SET_FORM_KEY;
import static com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet.parseChangeSet;
import static com.barda.sdk.plugin.sqlcommand.command.GuiConstants.MYSQL_COLUMN_DELIMITER;

import java.util.List;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSetRow;
import com.barda.sdk.plugin.sqlcommand.command.UpsertCommand;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;
import com.barda.sdk.util.MustacheHelper;

/**
 * 一个用于执行 MySQL upsert (插入或更新) 操作的命令类。
 * 继承自 {@link UpsertCommand}，并使用 MySQL 特定的列分隔符。
 */
public class MysqlUpsertCommand extends UpsertCommand {

    /**
     * 私有构造函数，仅在测试中使用。
     *
     * @param table 表名
     * @param insertChangeSet 包含插入操作的 {@link ChangeSet}
     * @param updateChangeSet 包含更新操作的 {@link ChangeSet}
     */
    @VisibleForTesting
    protected MysqlUpsertCommand(String table, ChangeSet insertChangeSet, ChangeSet updateChangeSet) {
        super(table, insertChangeSet, updateChangeSet, new FilterSet(), MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER);
    }

    /**
     * 私有构造函数，仅在 {@link MysqlUpsertCommand#from(Map)} 中使用。
     *
     * @param commandDetail 包含命令详细信息的 Map
     */
    protected MysqlUpsertCommand(Map<String, Object> commandDetail) {
        super(GuiSqlCommand.parseTable(commandDetail),
                parseChangeSet(commandDetail, INSERT_CHANGE_SET_FORM_KEY),
                parseChangeSet(commandDetail, UPDATE_CHANGE_SET_FORM_KEY),
                new FilterSet(),
                MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER
        );
    }

    /**
     * 从命令详细信息中创建一个 {@link MysqlUpsertCommand} 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return 一个新的 {@link MysqlUpsertCommand} 实例
     */
    public static MysqlUpsertCommand from(Map<String, Object> commandDetail) {
        return new MysqlUpsertCommand(commandDetail);
    }

    /**
     * 渲染 SQL 命令并返回渲染结果。
     *
     * @param requestMap 请求参数 Map
     * @return 渲染结果 {@link GuiSqlCommandRenderResult}
     */
    @Override
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {
        String renderedTable = MustacheHelper.renderMustacheString(table, requestMap);
        ChangeSetRow insertRow = insertChangeSet.render(requestMap);
        if (insertRow.isEmpty()) {
            throw new PluginException(INVALID_UPSERT_COMMAND, "UPSERT_DATA_EMPTY");
        }
        ChangeSetRow updateRow = updateChangeSet.render(requestMap);

        StringBuilder sb = new StringBuilder();
        List<Object> bindParams = newArrayList();

        boolean updateChangeEmpty = updateRow.isEmpty();
        appendTable(renderedTable, sb, updateChangeEmpty);
        appendInsertValues(insertRow, sb, bindParams);

        if (updateChangeEmpty) {
            return new GuiSqlCommandRenderResult(sb.toString(), bindParams);
        }

        appendUpsertKeyword(sb);
        appendUpdateValues(updateRow, sb, bindParams);

        return new GuiSqlCommandRenderResult(sb.toString(), bindParams);
    }
}

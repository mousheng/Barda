package com.barda.plugin.oracle.gui;

import static com.barda.plugin.oracle.gui.GuiConstants.COLUMN_DELIMITER_FRONT;
import static com.barda.sdk.plugin.sqlcommand.filter.FilterSet.parseFilterSet;

import java.util.Map;

import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;
import com.barda.sdk.plugin.sqlcommand.command.DeleteCommand;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet.RawFilterCondition;

/**
 * OracleDeleteCommand 类是 DeleteCommand 的子类，
 * 用于执行 Oracle 数据库的删除操作。
 */
public class OracleDeleteCommand extends DeleteCommand {

    /**
     * 构造函数，初始化 OracleDeleteCommand 实例。
     *
     * @param table 表名
     * @param filterSet 包含过滤条件的集合
     * @param allowMultiModify 是否允许多行修改
     */
    protected OracleDeleteCommand(String table, FilterSet filterSet, boolean allowMultiModify) {
        super(table, filterSet, allowMultiModify, COLUMN_DELIMITER_FRONT);
    }

    /**
     * 工厂方法，从 Map 类型的数据中创建 OracleDeleteCommand 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return OracleDeleteCommand 实例
     */
    public static DeleteCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        FilterSet filterSet = parseFilterSet(commandDetail);
        boolean allowMultiModify = GuiSqlCommand.parseAllowMultiModify(commandDetail);
        return new OracleDeleteCommand(table, filterSet, allowMultiModify);
    }

    /**
     * 重写 render 方法，在不允许多行修改的情况下，
     * 为 filterSet 添加 rownum = 1 的条件。
     */
    @Override
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {
        if (!allowMultiModify) {
            filterSet.addCondition(new RawFilterCondition("rownum", "=", 1));
        }

        return super.render(requestMap);
    }

    /**
     * 重写 renderLimit 方法，不执行任何操作。
     * Oracle 数据库不支持 LIMIT 子句，所以此处为空实现。
     */
    @Override
    protected void renderLimit(StringBuilder sb) {
        // do nothing
    }
}

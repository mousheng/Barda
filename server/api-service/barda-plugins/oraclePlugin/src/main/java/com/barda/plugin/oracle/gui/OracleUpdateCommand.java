package com.barda.plugin.oracle.gui;

import static com.barda.plugin.oracle.gui.GuiConstants.COLUMN_DELIMITER_FRONT;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.barda.sdk.plugin.sqlcommand.changeset.ChangeSet;
import com.barda.sdk.plugin.sqlcommand.command.UpdateCommand;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet;
import com.barda.sdk.plugin.sqlcommand.filter.FilterSet.RawFilterCondition;

/**
 * OracleUpdateCommand 类是 UpdateCommand 的子类，
 * 用于执行 Oracle 数据库的更新操作。
 */
public class OracleUpdateCommand extends UpdateCommand {

    /**
     * 私有构造函数，仅在本类中使用。
     *
     * @param commandDetail 更新命令的详细信息
     */
    private OracleUpdateCommand(Map<String, Object> commandDetail) {
        super(commandDetail, COLUMN_DELIMITER_FRONT, COLUMN_DELIMITER_FRONT);
    }

    /**
     * 仅供测试使用的受保护的构造函数，
     * 用于在测试中创建 OracleUpdateCommand 实例。
     *
     * @param table 表名
     * @param changeSet 包含更改的集合
     * @param filterSet 包含过滤条件的集合
     * @param allowMultiModify 是否允许多行修改
     */
    @VisibleForTesting
    protected OracleUpdateCommand(String table, ChangeSet changeSet, FilterSet filterSet, boolean allowMultiModify) {
        super(table, changeSet, filterSet, allowMultiModify, COLUMN_DELIMITER_FRONT, COLUMN_DELIMITER_FRONT);
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
     * 重写 appendLimit 方法，不执行任何操作。
     * Oracle 数据库不支持 LIMIT 子句，所以此处为空实现。
     */
    @Override
    protected void appendLimit(StringBuilder sb) {
        // do nothing
    }

    /**
     * 工厂方法，从 Map 类型的数据中创建 OracleUpdateCommand 实例。
     *
     * @param commandDetail 包含命令详细信息的 Map
     * @return OracleUpdateCommand 实例
     */
    public static OracleUpdateCommand from(Map<String, Object> commandDetail) {
        return new OracleUpdateCommand(commandDetail);
    }
}

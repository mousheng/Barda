package com.barda.sdk.plugin.sqlcommand;

import static com.barda.sdk.exception.PluginCommonError.INVALID_GUI_SETTINGS;
import static com.barda.sdk.plugin.common.constant.Constants.ALLOW_MULTI_MODIFY_KEY;
import static com.barda.sdk.plugin.common.constant.Constants.TABLE_KEY;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

/**
 * 定义了 GUI SQL 命令的接口。
 * 该接口包含了渲染 SQL 命令、解析表名、检查是否为插入命令等方法。
 */
public interface GuiSqlCommand {

    /**
     * 渲染 SQL 命令并返回 SQL 语句和绑定参数。
     *
     * @param requestMap  请求参数。
     * @return 渲染后的 SQL 命令。
     */
    GuiSqlCommandRenderResult render(Map<String, Object> requestMap);

    /**
     * 内部类，表示渲染后的 SQL 命令。
     */
    class GuiSqlCommandRenderResult {

        /**
         * SQL 语句。
         */
        private final String sql;

        /**
         * 绑定参数。
         */
        private final List<Object> bindParams;

        /**
         * 构造函数。
         *
         * @param sql  SQL 语句。
         * @param bindParams  绑定参数。
         */
        public GuiSqlCommandRenderResult(String sql, List<Object> bindParams) {
            this.sql = sql;
            this.bindParams = bindParams;
        }

        /**
         * 获取 SQL 语句。
         *
         * @return SQL 语句。
         */
        public String sql() {
            return sql;
        }

        /**
         * 获取绑定参数。
         *
         * @return 绑定参数。
         */
        public List<Object> bindParams() {
            return bindParams;
        }
    }

    /**
     * 从命令详情中解析并返回表名。
     *
     * @param commandDetail  命令详情。
     * @return 表名。
     */
    static String parseTable(Map<String, Object> commandDetail) {
        String table = MapUtils.getString(commandDetail, TABLE_KEY, null);
        if (StringUtils.isBlank(table)) {
            throw new PluginException(INVALID_GUI_SETTINGS, "GUI_FIELD_EMPTY");
        }
        return table;
    }

    /**
     * 从命令详情中解析并返回是否允许多次修改的标志。
     *
     * @param commandDetail  命令详情。
     * @return 是否允许多次修改的标志。
     */
    static boolean parseAllowMultiModify(Map<String, Object> commandDetail) {
        return MapUtils.getBoolean(commandDetail, ALLOW_MULTI_MODIFY_KEY, false);
    }

    /**
     * 检查是否为插入命令。
     *
     * @return true - 是插入命令，false - 不是插入命令。
     */
    boolean isInsertCommand();

    /**
     * 从命令中提取 Mustache 键。
     *
     * @return Mustache 键的集合。
     */
    Set<String> extractMustacheKeys();

    /**
     * 检查是否使用原始 SQL 渲染。
     *
     * @return true - 使用原始 SQL 渲染，false - 不使用原始 SQL 渲染。
     */
    default boolean isRenderWithRawSql() {
        return false;
    }

    /**
     * 获取 SQL 转义函数。
     *
     * @return SQL 转义函数。
     */
    default EscapeSql escapeStrFunc() {
        return s -> {
            throw new UnsupportedOperationException("This func should be implemented by each SQL dialect if needed");
        };
    }
}

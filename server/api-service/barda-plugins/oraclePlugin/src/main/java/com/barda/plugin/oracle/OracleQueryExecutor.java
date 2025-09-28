package com.barda.plugin.oracle;

import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_GET_STRUCTURE_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.plugin.common.sql.StructureParser.QUERY_STRUCTURE_SQL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.pf4j.Extension;

import com.barda.plugin.oracle.gui.OracleBulkInsertCommand;
import com.barda.plugin.oracle.gui.OracleBulkUpdateCommand;
import com.barda.plugin.oracle.gui.OracleDeleteCommand;
import com.barda.plugin.oracle.gui.OracleInsertCommand;
import com.barda.plugin.oracle.gui.OracleUpdateCommand;
import com.barda.plugin.sql.GeneralSqlExecutor;
import com.barda.plugin.sql.SqlBasedQueryExecutor;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceStructure;
import com.barda.sdk.models.DatasourceStructure.Table;
import com.barda.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;
import com.barda.sdk.plugin.common.sql.StructureParser;
import com.barda.sdk.plugin.sqlcommand.GuiSqlCommand;

import lombok.extern.slf4j.Slf4j;

/**
 * OracleQueryExecutor 类是 SqlBasedQueryExecutor 的扩展类，
 * 用于表示 Oracle 数据库的查询执行器。
 * 它使用了 Lombok 的 @Slf4j 注解来自动生成日志记录器，
 * 并使用了 @Extension 注解来标记为扩展类。
 */
@Slf4j
@Extension
public class OracleQueryExecutor extends SqlBasedQueryExecutor {

    /**
     * 公共构造函数，
     * 用于初始化 OracleQueryExecutor 实例。
     */
    public OracleQueryExecutor() {
        super(new GeneralSqlExecutor());
    }

    /**
     * 获取数据库的元数据。
     *
     * @param connection 数据库连接
     * @param connectionConfig 数据库连接配置
     * @return 数据库结构
     */
    @Nonnull
    @Override
    protected DatasourceStructure getDatabaseMetadata(Connection connection,
            SqlBasedDatasourceConnectionConfig connectionConfig) {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(QUERY_STRUCTURE_SQL)) {
            List<Table> tables = StructureParser.parseColumns(resultSet);
            return new DatasourceStructure(tables);
        } catch (SQLException throwable) {
            throw new PluginException(DATASOURCE_GET_STRUCTURE_ERROR, "DATASOURCE_GET_STRUCTURE_ERROR",
                    throwable.getMessage());
        }
    }

    /**
     * 重写 parseSqlCommand 方法，
     * 用于将 GUI 语句类型和详细信息解析为 GuiSqlCommand。
     *
     * @param guiStatementType GUI 语句类型
     * @param detail 详细信息
     * @return 解析后的 GuiSqlCommand
     */
    @Override
    protected GuiSqlCommand parseSqlCommand(String guiStatementType, Map<String, Object> detail) {
        return switch (guiStatementType.toUpperCase()) {
            case "INSERT" -> OracleInsertCommand.from(detail);
            case "UPDATE" -> OracleUpdateCommand.from(detail);
            case "DELETE" -> OracleDeleteCommand.from(detail);
            case "BULK_INSERT" -> OracleBulkInsertCommand.from(detail);
            case "BULK_UPDATE" -> OracleBulkUpdateCommand.from(detail);
            default -> throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_GUI_COMMAND_TYPE", guiStatementType);
        };
    }

}

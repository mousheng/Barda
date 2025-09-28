package com.barda.sdk.plugin.sqlcommand.command;

/**
 * 包含 GUI 相关的常量。
 * 该类是 final 类，不允许被继承。
 */
public final class GuiConstants {

    /**
     * 私有构造函数，防止该类被实例化。
     */
    private GuiConstants() {
    }

    /**
     * PostgreSQL 列名前缀和后缀分隔符。
     */
    public static final String POSTGRES_COLUMN_DELIMITER = "\"";

    /**
     * MySQL 列名前缀和后缀分隔符。
     */
    public static final String MYSQL_COLUMN_DELIMITER = "`";
}

package com.barda.plugin.mongo;

import com.barda.sdk.exception.PluginError;

/**
 * MongoPluginError是定义MongoDB插件相关错误的枚举。
 *
 * <p>
 * MongoPluginError枚举了MongoDB插件中可能发生的各种错误。
 * 它可以帮助开发者更好地处理和报告与MongoDB相关的错误。
 * </p>
 */
public enum MongoPluginError implements PluginError {

    /**
     * 指示在执行MongoDB命令时发生的错误。
     *
     * <p>
     * MONGO_EXECUTION_ERROR枚举值表示在执行MongoDB命令时发生了错误。
     * 这可能是由于网络问题、数据库问题或命令本身的语法错误等。
     * </p>
     */
    MONGO_EXECUTION_ERROR,

    /**
     * 指示在执行MongoDB命令时发生的命令相关的错误。
     *
     * <p>
     * MONGO_COMMAND_ERROR枚举值表示在执行MongoDB命令时发生了与命令本身相关的错误。
     * 这可能是由于命令的格式不正确、命令中使用的字段或值无效等。
     * </p>
     */
    MONGO_COMMAND_ERROR,
}

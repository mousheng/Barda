package com.barda.sdk.models;

import java.util.Locale;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

import com.barda.sdk.exception.PluginException;
import com.barda.sdk.util.LocaleUtils;

/**
 * 数据源测试结果类。
 * 包含了数据源测试的结果，包括成功、失败等信息。
 */
public class DatasourceTestResult {

    private static final DatasourceTestResult TEST_SUCCESS = new DatasourceTestResult(null);

    private final LocaleMessage localeErrorMsg;

    /**
     * 私有的构造函数。
     * 用于创建测试成功的结果。
     */
    private DatasourceTestResult(LocaleMessage localeErrorMsg) {
        this.localeErrorMsg = localeErrorMsg;
    }

    /**
     * 获取测试成功的结果。
     *
     * @return 一个测试成功的 {@link DatasourceTestResult} 对象。
     */
    public static DatasourceTestResult testSuccess() {
        return TEST_SUCCESS;
    }

    /**
     * 获取测试失败的结果。
     *
     * @param localeErrorMessage 本地化的错误消息。
     * @return 一个测试失败的 {@link DatasourceTestResult} 对象。
     */
    public static DatasourceTestResult testFail(@Nonnull LocaleMessage localeErrorMessage) {
        return new DatasourceTestResult(localeErrorMessage);
    }

    /**
     * 获取测试失败的结果。
     *
     * @param rawErrorMsg 原始的错误消息。
     * @return 一个测试失败的 {@link DatasourceTestResult} 对象。
     */
    public static DatasourceTestResult testFail(@Nonnull String rawErrorMsg) {
        return new DatasourceTestResult(new LocaleMessage("DATASOURCE_TEST_GENERIC_ERROR", rawErrorMsg));
    }

    /**
     * 获取测试失败的结果。
     *
     * @param e 导致测试失败的异常。
     * @return 一个测试失败的 {@link DatasourceTestResult} 对象。
     */
    public static DatasourceTestResult testFail(Throwable e) {
        if (e instanceof PluginException pluginException) {
            LocaleMessage localeMessage = new LocaleMessage(pluginException.getMessageKey(), pluginException.getArgs());
            return new DatasourceTestResult(localeMessage);
        }

        if (e instanceof TimeoutException) {
            return new DatasourceTestResult(new LocaleMessage("DATASOURCE_TEST_TIMEOUT_ERROR"));
        }

        return new DatasourceTestResult(new LocaleMessage("DATASOURCE_TEST_GENERIC_ERROR", e.getMessage()));
    }

    /**
     * 判断测试是否成功。
     *
     * @return 如果测试成功，返回 true，否则返回 false。
     */
    public boolean isSuccess() {
        return localeErrorMsg == null;
    }

    /**
     * 获取无效的消息。
     *
     * @param locale 本地化的语言。
     * @return 本地化的无效消息。
     */
    public String getInvalidMessage(Locale locale) {
        return LocaleUtils.getMessage(locale, localeErrorMsg);
    }
}

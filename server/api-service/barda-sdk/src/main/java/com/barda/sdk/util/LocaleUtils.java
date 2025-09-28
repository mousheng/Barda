package com.barda.sdk.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.barda.sdk.constants.GlobalContext;
import com.barda.sdk.models.LocaleMessage;

import lombok.extern.slf4j.Slf4j;
import reactor.util.context.ContextView;

/**
 * 一个国际化（i18n）的实用工具类。
 * 该类提供了一系列静态方法来处理国际化相关的操作。
 */
@Slf4j
public final class LocaleUtils {

    /**
     * 私有构造函数，防止该类被实例化。
     */
    private LocaleUtils() {
    }

    /**
     * 获取国际化消息。
     *
     * @param locale       本地化
     * @param localeMessage 本地化消息
     * @return 国际化消息
     */
    public static String getMessage(Locale locale, LocaleMessage localeMessage) {
        return getMessage(locale, localeMessage.messageKey(), localeMessage.args());
    }

    /**
     * 获取国际化消息。
     *
     * @param locale 本地化
     * @param key    消息键
     * @param args   消息参数
     * @return 国际化消息
     */
    public static String getMessage(Locale locale, String key, Object... args) {

        try {
            ResourceBundle eeBundle = ResourceBundle.getBundle("locale_ee", locale);
            if (StringUtils.equals(eeBundle.getLocale().getLanguage(), locale.getLanguage())
                    && eeBundle.containsKey(key.trim())) {
                return new MessageFormat(eeBundle.getString(key.trim())).format(args);
            }
        } catch (Exception e) {
            // 忽略异常
        }

        ResourceBundle bundle = ResourceBundle.getBundle("locale", locale);
        if (!bundle.containsKey(key.trim())) {
            log.error("message key not exist ,  {} - {}", locale, key);
            return bundle.getString("INTERNAL_SERVER_ERROR");
        }
        return new MessageFormat(bundle.getString(key.trim())).format(args);
    }

    /**
     * 获取上下文视图中的本地化。
     *
     * @param contextView 上下文视图
     * @return 本地化
     */
    public static Locale getLocale(ContextView contextView) {
        return contextView.getOrDefault(GlobalContext.CLIENT_LOCALE, getDefaultLocale());
    }

    /**
     * 获取默认的本地化。
     *
     * @return 默认的本地化
     */
    public static Locale getDefaultLocale() {
        return Locale.ENGLISH;
    }
}
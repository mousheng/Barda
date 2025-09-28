package com.barda.sdk.util;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 日期时间工具类。
 * 该类提供了一组静态方法来操作和格式化日期时间。
 */
public class DateTimeUtils {

    /**
     * 日期时间格式化器。
     * 格式为 "yyyy-MM-dd HH:mm:ss"。
     */
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 格式化日期。
     *
     * @param date  要格式化的日期。
     * @return 格式化后的日期字符串。
     */
    public static String format(Date date) {
        return DateFormatUtils.format(date, "yyyy-MM-dd");
    }

    /**
     * 将 Date 转换为 Instant。
     *
     * @param date  要转换的 Date。
     * @return 转换后的 Instant。
     */
    public static Instant toInstant(Date date) {
        return Instant.ofEpochMilli(date.getTime());
    }

    /**
     * 将 Object 转换为 Instant。
     *
     * @param o  要转换的 Object。
     * @return 转换后的 Instant，如果 Object 既不是 Instant 也不是 Date，则返回 null。
     */
    public static Instant toInstant(Object o) {
        if (o instanceof Instant instant) {
            return instant;
        }
        if (o instanceof Date date) {
            return toInstant(date);
        }
        return null;
    }
}

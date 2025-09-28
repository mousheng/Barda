package com.barda.sdk.util;

import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.IMAGE_GIF;
import static org.springframework.http.MediaType.IMAGE_JPEG;
import static org.springframework.http.MediaType.IMAGE_PNG;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import com.google.common.base.Preconditions;

/**
 * 一个媒体类型（MediaType）的实用工具类。
 * 该类提供了一系列静态方法来处理媒体类型相关的操作。
 */
public class MediaTypeUtils {

    /**
     * 解析文件名并获取媒体类型。
     *
     * @param filename 文件名
     * @return 媒体类型
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static MediaType parse(String filename) {
        return parse(filename, APPLICATION_OCTET_STREAM);
    }

    /**
     * 解析文件名并获取媒体类型。
     *
     * @param filename        文件名
     * @param defaultContentType 默认的媒体类型
     * @return 媒体类型
     */
    @Nullable
    public static MediaType parse(String filename, @Nullable MediaType defaultContentType) {
        Preconditions.checkArgument(StringUtils.isNotBlank(filename));
        String[] split = filename.split("\\.");
        return getMediaType(split[split.length - 1], defaultContentType);
    }

    /**
     * 获取指定文件类型对应的媒体类型。
     *
     * @param fileType 文件类型
     * @return 媒体类型
     */
    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static MediaType getMediaType(String fileType) {
        return getMediaType(fileType, APPLICATION_OCTET_STREAM);
    }

    /**
     * 获取指定文件类型对应的媒体类型。
     *
     * @param fileType          文件类型
     * @param defaultContentType 默认的媒体类型
     * @return 媒体类型
     */
    @Nullable
    public static MediaType getMediaType(String fileType, @Nullable MediaType defaultContentType) {
        return switch (fileType) {
            case "jpg", "jpeg" -> IMAGE_JPEG;
            case "gif" -> IMAGE_GIF;
            case "png" -> IMAGE_PNG;
            case "pdf" -> APPLICATION_PDF;
            case "svg" -> new MediaType("image", "svg+xml");
            default -> defaultContentType;
        };
    }
}

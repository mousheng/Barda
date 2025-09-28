package com.barda.plugin.restapi.helpers;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

/**
 * 这是一个提供内容类型帮助功能的实用类。
 * 它包含一些静态方法来检查内容类型是否为二进制、图片或 JSON。
 */
public final class ContentTypeHelper {

    /**
     * 私有构造函数，防止实例化。
     */
    private ContentTypeHelper() {
    }

    /**
     * 包含二进制数据类型列表。
     */
    private static final Set<String> BINARY_DATA_TYPES = Set.of("application/zip",
            "application/octet-stream",
            "application/pdf",
            "application/pkcs8",
            "application/x-binary");

    /**
     * 检查内容类型是否为二进制。
     *
     * @param contentType 要检查的媒体类型
     * @return 如果内容类型为二进制，则返回 true；否则返回 false
     */
    public static boolean isBinary(MediaType contentType) {
        return BINARY_DATA_TYPES.contains(contentType.toString());
    }

    /**
     * 检查内容类型是否为图片。
     *
     * @param contentType 要检查的媒体类型
     * @return 如果内容类型为图片，则返回 true；否则返回 false
     */
    public static boolean isPicture(MediaType contentType) {
        return MediaType.IMAGE_GIF.equals(contentType) ||
                MediaType.IMAGE_JPEG.equals(contentType) ||
                MediaType.IMAGE_PNG.equals(contentType);
    }

    /**
     * 检查内容类型是否为 JSON。
     *
     * @param mediaType 要检查的媒体类型
     * @return 如果内容类型为 JSON，则返回 true；否则返回 false
     */
    public static boolean isJson(MediaType mediaType) {
        return StringUtils.equalsIgnoreCase("application", mediaType.getType())
                && (StringUtils.equalsIgnoreCase(mediaType.getSubtype(), "json")
                || StringUtils.equals(mediaType.getSubtype(), "x-ndjson")
                || StringUtils.contains(mediaType.getSubtype(), "+json")
                || isSpecialJson(mediaType));
    }

    /**
     * 检查内容类型是否为特殊的 JSON。
     *
     * @param mediaType 要检查的媒体类型
     * @return 如果内容类型为特殊的 JSON，则返回 true；否则返回 false
     */
    public static boolean isSpecialJson(MediaType mediaType) {
        return StringUtils.contains(mediaType.getSubtype(), "-json");
    }

    /**
     * 从 HTTP 头部中解析内容类型。
     *
     * @param allHeaders 包含 HTTP 头部的 Map
     * @return 解析出的内容类型字符串
     */
    public static String parseContentType(Map<String, String> allHeaders) {
        return allHeaders.entrySet()
                .stream()
                .filter(it -> HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(it.getKey()))
                .map(Entry::getValue)
                .findFirst()
                .orElse("");
    }

    /**
     * 检查提供的字符串是否为有效的内容类型。
     *
     * @param requestContentType 要检查的字符串
     * @return 如果字符串为有效的内容类型，则返回 true；否则返回 false
     */
    public static boolean isValidContentType(String requestContentType) {
        if (StringUtils.isBlank(requestContentType)) {
            return true;
        }

        try {
            MediaType.valueOf(requestContentType);
        } catch (InvalidMediaTypeException e) {
            return false;
        }

        return true;
    }

    /**
     * 检查提供的字符串是否为 JSON 内容类型。
     *
     * @param contentType 要检查的字符串
     * @return 包含是否为 JSON 内容类型和是否为特殊的 JSON 的 Pair
     */
    public static Pair<Boolean, Boolean> isJsonContentType(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return Pair.of(false, false);
        }
        MediaType mediaType = MediaType.valueOf(contentType);
        return Pair.of(isJson(mediaType), isSpecialJson(mediaType));
    }
}

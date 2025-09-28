package com.barda.sdk.plugin.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;

import com.barda.sdk.models.Property;
import com.barda.sdk.plugin.restapi.DataUtils;

/**
 * 帮助类，用于处理原始HTTP请求的辅助功能。
 */
public class RawHttpHandlerHelper {

    /**
     * 单例DataUtils的实例。
     */
    private static final DataUtils DATA_UTILS = DataUtils.getInstance();


    /**
     * 构建BodyInserter以将请求正文发送到HTTP请求中。
     *
     * @param httpMethod HTTP方法
     * @param isEncodeParams 是否对参数进行URL编码
     * @param requestContentType 请求的Content-Type
     * @param queryBody 查询正文
     * @param bodyFormData 表单数据
     * @return 构建的BodyInserter
     */
    public static BodyInserter<?, ? super ClientHttpRequest> buildBodyInserter(HttpMethod httpMethod,
            boolean isEncodeParams,
            String requestContentType,
            String queryBody,
            List<Property> bodyFormData) {

        // 如果是GET方法，返回空字节数组
        if (HttpMethod.GET.equals(httpMethod)) {
            return BodyInserters.fromValue(new byte[0]);
        }

        // 如果Content-Type为空，返回空字节数组
        if (isNoneContentType(requestContentType)) {
            return BodyInserters.fromValue(new byte[0]);
        }

        // 如果Content-Type为JSON，解析并返回JSON正文
        if (isJsonContentType(requestContentType)) {
            return BodyInserters.fromValue(DataUtils.parseJsonBody(queryBody));
        }

        // 如果Content-Type为application/x-www-form-urlencoded或multipart/form-data，构建并返回BodyInserter
        if (MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(requestContentType)
                || MediaType.MULTIPART_FORM_DATA_VALUE.equals(requestContentType)) {
            return DATA_UTILS.buildBodyInserter(bodyFormData, requestContentType, isEncodeParams);
        }

        // 其他情况，返回原始查询正文
        return BodyInserters.fromValue(queryBody);
    }

    /**
     * 检查Content-Type是否为JSON。
     *
     * @param requestContentType 请求的Content-Type
     * @return true表示是JSON，false表示不是JSON
     */
    @SuppressWarnings("deprecation")
    private static boolean isJsonContentType(String requestContentType) {
        return MediaType.APPLICATION_JSON_VALUE.equals(requestContentType)
                || MediaType.APPLICATION_JSON_UTF8_VALUE.equals(requestContentType)
                || MediaType.APPLICATION_PROBLEM_JSON_VALUE.equals(requestContentType)
                || MediaType.APPLICATION_PROBLEM_JSON_UTF8_VALUE.equals(requestContentType);
    }

    /**
     * 检查Content-Type是否为空。
     *
     * @param requestContentType 请求的Content-Type
     * @return true表示为空，false表示不为空
     */
    private static boolean isNoneContentType(String requestContentType) {
        return StringUtils.isBlank(requestContentType);
    }

    /**
     * 从HTTP头部中解析Content-Type。
     *
     * @param allHeaders 所有HTTP头部
     * @return 解析出的Content-Type
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
     * 从属性列表中构建HTTP头部。
     *
     * @param queryHeaders 属性列表
     * @return 构建出的HTTP头部
     */
    public static Map<String, String> buildHeaders(List<Property> queryHeaders) {
        return queryHeaders.stream()
                .filter(it -> StringUtils.isNotBlank(it.getKey()) && StringUtils.isNotBlank(it.getValue()))
                .collect(Collectors.toUnmodifiableMap(property -> property.getKey().trim().toLowerCase(),
                        Property::getValue,
                        (oldValue, newValue) -> newValue));
    }

    /**
     * 检查Content-Type是否有效。
     *
     * @param requestContentType 请求的Content-Type
     * @return true表示有效，false表示无效
     */
    public static boolean isValidContentType(String requestContentType) {
        if (StringUtils.isEmpty(requestContentType)) {
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
     * 从路径和参数中构建URI。
     *
     * @param path 路径
     * @param params 参数列表
     * @return 构建出的URI
     * @throws URISyntaxException 如果URI构建失败
     */
    public static URI buildUri(String path, List<Property> params) throws URISyntaxException {
        URI uri;
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        builder.uri(new URI(path));
        if (params != null) {
            params.forEach(property -> builder.queryParam(property.getKey(), property.getValue()));
        }
        uri = builder.build(true).toUri();
        return uri;
    }
}

package com.barda.sdk.plugin.http;

import static com.barda.sdk.util.MustacheHelper.renderMustacheJsonString;
import static com.barda.sdk.util.MustacheHelper.renderMustacheString;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

import com.barda.sdk.models.Property;

import lombok.Getter;
import lombok.Setter;

/**
 * 表示原始HTTP请求类，包含HTTP方法、请求体、路径、参数、头信息和表单数据。
 */
@Setter
@Getter
public class RawHttpRequest {

    /**
     * HTTP方法
     */
    private final HttpMethod httpMethod;

    /**
     * 请求体内容
     */
    private String body;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 请求参数列表
     */
    private List<Property> params;

    /**
     * 请求头信息列表
     */
    private List<Property> headers;

    /**
     * 请求表单数据列表
     */
    private List<Property> bodyFormData;

    /**
     * 构造一个新的原始HTTP请求对象。
     *
     * @param httpMethod  HTTP方法
     * @param body        请求体内容
     * @param path        请求路径
     * @param params      请求参数列表
     * @param headers     请求头信息列表
     * @param bodyFormData 请求表单数据列表
     */
    protected RawHttpRequest(HttpMethod httpMethod, String body, String path,
            List<Property> params, List<Property> headers, List<Property> bodyFormData) {
        this.httpMethod = httpMethod;
        this.body = body;
        this.path = path;
        this.params = params;
        this.headers = headers;
        this.bodyFormData = bodyFormData;
    }

    /**
     * 渲染请求中的参数，将Mustache模板变量替换为实际值。
     *
     * @param paramMap 参数映射
     */
    public void renderParams(Map<String, Object> paramMap) {
        body = renderMustacheJsonString(body, paramMap);
        path = renderMustacheString(path, paramMap);
        params = ListUtils.emptyIfNull(params).stream()
                .map(property -> render(property, paramMap))
                .toList();
        headers = ListUtils.emptyIfNull(headers).stream()
                .map(property -> render(property, paramMap))
                .toList();
        bodyFormData = ListUtils.emptyIfNull(bodyFormData).stream()
                .map(property -> render(property, paramMap))
                .toList();
    }

    /**
     * 渲染单个属性，将Mustache模板变量替换为实际值。
     *
     * @param property 属性
     * @param paramMap 参数映射
     * @return 渲染后的属性
     */
    private Property render(Property property, Map<String, Object> paramMap) {
        String key = renderMustacheString(property.getKey(), paramMap);
        String value = renderMustacheString(property.getValue(), paramMap);
        return new Property(key, value, property.getType());
    }

    /**
     * 检查请求数据是否无效。
     *
     * @return 如果HTTP方法为null或路径为空，返回true，否则返回false
     */
    public boolean hasInvalidData() {
        return httpMethod == null || StringUtils.isBlank(path);
    }
}
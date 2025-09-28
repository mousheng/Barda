/**
 * Copyright 2021 Appsmith Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 */

// copied and adapted for rest api request
package com.barda.sdk.plugin.restapi;

import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_ARGUMENT_ERROR;
import static com.barda.sdk.util.MustacheHelper.renderMustacheJson;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Streams;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.exception.ServerException;
import com.barda.sdk.models.Property;
import com.barda.sdk.models.RestBodyFormFileData;
import com.barda.sdk.util.ExceptionUtils;

import reactor.core.publisher.Mono;

/**
 * 包含处理数据相关的实用方法的类。
 */
public class DataUtils {

    private static DataUtils dataUtils;
    private final ObjectMapper objectMapper;

    /**
     * 定义了多部分表单数据类型。
     */
    public enum MultipartFormDataType {
        TEXT,
        FILE
    }

    private DataUtils() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * 获取单例的 DataUtils 实例。
     *
     * @return DataUtils 单例
     */
    public static DataUtils getInstance() {
        if (dataUtils == null) {
            dataUtils = new DataUtils();
        }
        return dataUtils;
    }

    /**
     * 构建 BodyInserter 以将数据添加到 HTTP 请求中。
     *
     * @param body 要添加的数据
     * @param contentType HTTP 请求的 Content-Type
     * @param encodeParamsToggle 指示是否对参数进行 URL 编码
     * @return 构建的 BodyInserter
     */
    public BodyInserter<?, ? super ClientHttpRequest> buildBodyInserter(List<Property> body, String contentType,
            boolean encodeParamsToggle) {
        if (body == null) {
            return BodyInserters.fromValue(new byte[0]);
        }
        switch (contentType) {
            case MediaType.APPLICATION_FORM_URLENCODED_VALUE:
                String formData = parseFormData(body, encodeParamsToggle);
                if ("".equals(formData)) {
                    return BodyInserters.fromValue(new byte[0]);
                }
                return BodyInserters.fromValue(formData);
            case MediaType.MULTIPART_FORM_DATA_VALUE:
                return parseMultipartFileData(body);
            default:
                return BodyInserters.fromValue(body);
        }
    }

    /**
     * 解析 JSON 格式的 body。
     *
     * @param body 要解析的 body
     * @return 解析后的结果
     */
    public static Object parseJsonBody(Object body) {
        if (body instanceof String str) {
            if ("" == body) {
                return new byte[0];
            }
            return renderMustacheJson(str, Collections.emptyMap());
        }

        return body;
    }

    /**
     * 解析表单数据并返回 URL 编码的字符串。
     *
     * @param bodyFormData 要解析的表单数据
     * @param encodeParamsToggle 指示是否对参数进行 URL 编码
     * @return 解析后的 URL 编码的字符串
     */
    public String parseFormData(List<Property> bodyFormData, boolean encodeParamsToggle) {
        if (bodyFormData == null || bodyFormData.isEmpty()) {
            return "";
        }

        return bodyFormData
                .stream()
                .filter(property -> property.getKey() != null)
                .map(property -> {
                    String key = property.getKey();
                    String value = property.getValue();

                    if (encodeParamsToggle) {
                        try {
                            value = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
                        } catch (UnsupportedEncodingException e) {
                            throw new UnsupportedOperationException(e);
                        }
                    }

                    return key + "=" + value;
                })
                .collect(Collectors.joining("&"));

    }

    /**
     * 解析多部分表单数据并返回 BodyInserter。
     *
     * @param bodyFormData 要解析的多部分表单数据
     * @return 解析后的 BodyInserter
     */
    public BodyInserter<?, ? super ClientHttpRequest> parseMultipartFileData(List<Property> bodyFormData) {
        if (CollectionUtils.isEmpty(bodyFormData)) {
            return BodyInserters.fromValue(new byte[0]);
        }

        return (BodyInserter<?, ClientHttpRequest>) (outputMessage, context) ->
                Mono.defer(() -> {
                    MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
                    bodyFormData.stream()
                            .filter(it -> StringUtils.isNotBlank(it.getKey()))
                            .forEach(property -> {
                                String key = property.getKey();
                                if (!property.isMultipartFileType()) {
                                    bodyBuilder.part(key, property.getValue());
                                    return;
                                }

                                populateMultiformFileData(bodyBuilder, property);

                            });

                    return BodyInserters.fromMultipartData(bodyBuilder.build())
                            .insert(outputMessage, context);
                });
    }

    /**
     * 填充多部分表单数据。
     *
     * @param bodyBuilder 多部分表单构建器
     * @param property 要填充的数据
     */
    private void populateMultiformFileData(MultipartBodyBuilder bodyBuilder, Property property) {
        parseMultipartFormDataList(property)
                .forEach(multipartFormData -> {
                    String name = multipartFormData.getName();
                    String data = multipartFormData.getData().trim();
                    byte[] decodedValue;
                    try {
                        decodedValue = Base64.getDecoder().decode(data);
                    } catch (Exception e) {
                        throw ExceptionUtils.wrapException(DATASOURCE_ARGUMENT_ERROR, "FAIL_TO_PARSE_BASE64_STRING", e);
                    }
                    bodyBuilder.part(property.getKey(), decodedValue)
                            .filename(name);
                });
    }

    /**
     * 解析多部分表单数据列表。
     *
     * @param property 要解析的数据
     * @return 解析后的多部分表单数据列表
     */
    private List<MultipartFormData> parseMultipartFormDataList(Property property) {
        if (property instanceof RestBodyFormFileData restBodyFormFileData) {
            return restBodyFormFileData.getFileData();
        }
        throw new ServerException("invalid data type for MultipartForm");
    }

    /**
     * 将字符串转换为多部分表单值。
     *
     * @param fileValue 要转换的字符串
     * @param paramMap 模板参数
     * @return 转换后的多部分表单值列表
     */
    public static List<MultipartFormData> convertToMultiformFileValue(String fileValue, Map<String, Object> paramMap) {
        try {
            JsonNode jsonValue = renderMustacheJson(fileValue, paramMap);
            if (jsonValue.isObject()) {
                return List.of(parseMultipartFormData(jsonValue));
            }

            if (jsonValue.isArray()) {
                return Streams.stream(jsonValue)
                        .filter(JsonNode::isObject)
                        .map(DataUtils::parseMultipartFormData)
                        .toList();
            }
        } catch (Throwable e) {
            throw ExceptionUtils.wrapException(DATASOURCE_ARGUMENT_ERROR, "CONTENT_PARSE_ERROR", e);
            // ignore
        }
        throw new PluginException(DATASOURCE_ARGUMENT_ERROR, "CONTENT_PARSE_ERROR");
    }

    /**
     * 解析多部分表单数据。
     *
     * @param jsonObject 要解析的 JSON 对象
     * @return 解析后的多部分表单数据
     */
    @Nonnull
    private static MultipartFormData parseMultipartFormData(JsonNode jsonObject) {
        MultipartFormData result = new MultipartFormData();
        JsonNode data = jsonObject.get("data");
        if (!data.isTextual()) {
            throw new PluginException(DATASOURCE_ARGUMENT_ERROR, "MULTIFORM_DATA_IS_NOT_STRING");
        }
        result.setData(data.asText());

        JsonNode name = jsonObject.get("name");
        if (!name.isTextual()) {
            throw new PluginException(DATASOURCE_ARGUMENT_ERROR, "MULTIFORM_NAME_IS_NOT_STRING");
        }
        result.setName(name.asText());
        return result;
    }
}

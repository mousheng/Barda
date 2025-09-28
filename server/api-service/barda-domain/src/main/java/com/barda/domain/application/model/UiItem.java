package com.barda.domain.application.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

/**
 * 该记录类表示 UI 项。
 *
 * <p>记录类包含以下字段：
 * <ul>
 *     <li>{@code compType}：表示组件类型。</li>
 *     <li>{@code comp}：表示组件。</li>
 * </ul>
 *
 * <p>此外，该记录类包含一个名为 {@code Comp} 的嵌套记录类，
 * 该类表示 UI 项中的组件。
 *
 * <p>该记录类使用了 Lombok 的 {@code @Builder} 注解来生成构建器模式的构造器。
 * 构建器模式可以让您更方便地创建和修改 {@code UiItem} 对象的实例。
 *
 * <p>该记录类还包含一个名为 {@code JsonCreator} 的注解，
 * 该注解用于在反序列化 JSON 对象时创建 {@code UiItem} 对象的实例。
 * 该注解使用了 Lombok 的 {@code @JsonProperty} 注解来指定 JSON 属性的名称。
 *
 * <p>
 * 请注意，此处的注释是根据中文翻译的，您可以根据您的需求进行修改。
 */
@Builder
public record UiItem(String compType, Comp comp) {

    /**
     * 该构造器方法用于在反序列化 JSON 对象时创建 {@code UiItem} 对象的实例。
     *
     * @param compType 组件类型
     * @param comp 组件
     */
    @JsonCreator
    public UiItem(@JsonProperty("compType") String compType, @JsonProperty("comp") Comp comp) {
        this.compType = compType;
        this.comp = comp;
    }

    /**
     * 该嵌套记录类表示 UI 项中的组件。
     *
     * <p>嵌套记录类包含以下字段：
     * <ul>
     *     <li>{@code appId}：表示应用程序的 ID。</li>
     * </ul>
     */
    public record Comp(String appId) {
    }
}

package com.barda.domain.application.model;

import java.util.Arrays;

import lombok.Getter;

/**
 * 该枚举表示应用程序的类型。
 *
 * <p>枚举包括以下类型：
 * <ul>
 *     <li>{@link #APPLICATION}：表示应用程序。</li>
 *     <li>{@link #MODULE}：表示模块。</li>
 *     <li>{@link #COMPOUND_APPLICATION}：表示复合应用程序。</li>
 * </ul>
 *
 * <p>此外，该枚举包含一个名为 {@code fromValue} 的静态方法，
 * 该方法用于根据给定的整型值返回相应的枚举值。
 * 如果找不到匹配的枚举值，则返回 {@code null}。
 *
 * <p>
 * 请注意，此处的注释是根据中文翻译的，您可以根据您的需求进行修改。
 */
@Getter
public enum ApplicationType {
    APPLICATION(1),
    MODULE(2),
    COMPOUND_APPLICATION(3);

    private final int value;

    ApplicationType(int value) {
        this.value = value;
    }

    /**
     * 根据给定的整型值返回相应的 {@link ApplicationType} 枚举值。
     * 如果找不到匹配的枚举值，则返回 {@code null}。
     *
     * @param value 要查找的整型值
     * @return 与给定值匹配的 {@link ApplicationType} 枚举值，如果找不到匹配的值，则返回 {@code null}
     */
    public static ApplicationType fromValue(int value) {
        return Arrays.stream(values())
                .filter(it -> it.value == value)
                .findFirst()
                .orElse(null);
    }

}

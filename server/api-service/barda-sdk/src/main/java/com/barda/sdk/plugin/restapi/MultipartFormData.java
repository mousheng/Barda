package com.barda.sdk.plugin.restapi;

import static com.google.common.base.Strings.nullToEmpty;
import lombok.Setter;

/**
 * 多部分表单数据类。
 * 该类用于表示多部分表单中的单个数据项。
 */
@Setter
public class MultipartFormData {

    /**
     * 表单项的名称。
     */
    private String name;

    /**
     * 表单项的数据。
     */
    private String data;

    /**
     * 获取表单项的名称。
     * 如果名称为 null，则返回空字符串。
     *
     * @return 表单项的名称
     */
    public String getName() {
        return nullToEmpty(name);
    }

    /**
     * 获取表单项的数据。
     *
     * @return 表单项的数据
     */
    public String getData() {
        return data;
    }
}

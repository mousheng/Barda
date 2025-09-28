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
package com.barda.plugin.mongo.constants;

import java.util.regex.Pattern;

/**
 * 该枚举类用于列出在智能替换过程中需要特殊处理的 MongoDB 数据类型。
 * 例如，通常 {"name": "Chris"} 是一个有效的 BSON 文档，但 ObjectId 类型的值需要特殊处理：
 * {"_id": ObjectId("xyz")} 有效，而 {"_id": "ObjectId(\"xyz\")"} 无效。
 */
public enum MongoSpecialDataTypes {

    ObjectId,
    ISODate,
    Date,
    // NumberLong,
    // NumberDecimal,
    Timestamp {
        /**
         * 某些数据类型需要将其参数包裹在引号中，而其他数据类型则不需要。
         * 例如，ObjectId("xyz") 有效，而 Timestamp(1234, 1) 无需引号。
         *
         * @return 是否需要将参数包裹在引号中
         */
        @Override
        public boolean isQuotesRequiredAroundParameter() {
            return false;
        }
    },
    // BinData - 尚不确定

    ;

    private static final String MONGODB_SPECIAL_TYPE_INSIDE_QUOTES_REGEX_TEMPLATE = """
            (\\"(E\\(([\\s'"]*(.*?)[\\s'"]*)?\\))\\")""";
    private final Pattern regexPattern;

    MongoSpecialDataTypes() {
        String regex = MONGODB_SPECIAL_TYPE_INSIDE_QUOTES_REGEX_TEMPLATE.replace("E", name());
        regexPattern = Pattern.compile(regex);
    }

    /**
     * 某些数据类型需要将其参数包裹在引号中，而其他数据类型则不需要。
     * 例如，ObjectId("xyz") 有效，而 Timestamp(1234, 1) 无需引号。
     *
     * @return 是否需要将参数包裹在引号中
     */
    public boolean isQuotesRequiredAroundParameter() {
        return true;
    }

    public Pattern getRegexPattern() {
        return regexPattern;
    }
}
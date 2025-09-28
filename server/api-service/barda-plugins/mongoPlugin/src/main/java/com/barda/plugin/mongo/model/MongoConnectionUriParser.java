package com.barda.plugin.mongo.model;

import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_ARGUMENT_ERROR;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 用于解析和提取 MongoDB 连接 URI 的类。
 */
public final class MongoConnectionUriParser {

    /**
     * 正则表达式，用于匹配以下两种模式：
     * - mongodb+srv://user:pass@some-url/some-db...
     * - mongodb://user:pass@some-url:port,some-url:port,.../some-db...
     * 正则表达式分组如下：(mongodb+srv://)(user):(pass)@(some-url)/(some-db...)?(params...)
     *
     * ^(mongodb(?:\\+srv)?://)(?:(.+):(.+)@)?([^\\/\\?]+)\\/?([^\?]+)?\\??(.+)?$
     */
    public static final String MONGO_URI_REGEX = "^(mongodb(?:\\+srv)?://)(?:(.+):(.+)@)?([^/?]+)/?([^?]+)?\\??(.+)?$";

    private static final Pattern PATTERN = Pattern.compile(MONGO_URI_REGEX);

    private static final int REGEX_GROUP_HEAD = 1;

    private static final int REGEX_GROUP_USERNAME = 2;

    private static final int REGEX_GROUP_PASSWORD = 3;

    private static final int REGEX_HOST_PORT = 4;

    private static final int REGEX_GROUP_DBNAME = 5;

    private static final int REGEX_GROUP_TAIL = 6;

    private static final String KEY_USERNAME = "username";

    private static final String KEY_PASSWORD = "password";

    private static final String KEY_HOST_PORT = "hostPort";

    private static final String KEY_URI_HEAD = "uriHead";

    private static final String KEY_URI_TAIL = "uriTail";

    private static final String KEY_URI_DBNAME = "dbName";

    /**
     * 验证给定的 URI 是否是有效的 MongoDB 连接 URI。
     *
     * @param uri 要验证的 URI
     * @return 如果 URI 有效，返回 true；否则返回 false
     */
    public static boolean isValid(String uri) {
        return PATTERN.asMatchPredicate().test(uri);
    }

    /**
     * 从 MongoDB 连接 URI 中提取信息。
     *
     * @param uri 要提取信息的 URI
     * @return 包含提取信息的 Map，如果 URI 无效，返回 null
     */
    public static Map<String, String> extractInfoFromConnectionStringURI(String uri) {
        if (!uri.matches(MONGO_URI_REGEX)) {
            return null;
        }

        Matcher matcher = PATTERN.matcher(uri);
        if (matcher.find()) {
            Map<String, String> extractedInfoMap = new HashMap<>();
            extractedInfoMap.put(KEY_URI_HEAD, matcher.group(REGEX_GROUP_HEAD));
            extractedInfoMap.put(KEY_USERNAME, matcher.group(REGEX_GROUP_USERNAME));
            extractedInfoMap.put(KEY_PASSWORD, matcher.group(REGEX_GROUP_PASSWORD));
            extractedInfoMap.put(KEY_HOST_PORT, matcher.group(REGEX_HOST_PORT));
            extractedInfoMap.put(KEY_URI_DBNAME, matcher.group(REGEX_GROUP_DBNAME));
            extractedInfoMap.put(KEY_URI_TAIL, matcher.group(REGEX_GROUP_TAIL));
            return extractedInfoMap;
        }

        return null;
    }

    /**
     * 从 MongoDB 连接字符串中解析数据库名称的工具方法。
     *
     * @param uri MongoDB 连接字符串
     * @return 数据库名称
     * @throws PluginException 如果提供的 URI 无效或数据库名称为空，则抛出异常
     */
    public static String parseDatabaseFrom(String uri) {
        Map<String, String> extractedInfo = extractInfoFromConnectionStringURI(uri);
        if (extractedInfo == null) {
            throw ofPluginException(DATASOURCE_ARGUMENT_ERROR, "INVALID_MONGODB_URI");
        }

        String database = extractedInfo.get(KEY_URI_DBNAME);
        if (StringUtils.isBlank(database)) {
            throw ofPluginException(DATASOURCE_ARGUMENT_ERROR, "MONGODB_DATABASE_EMPTY");
        }
        return database;
    }

}

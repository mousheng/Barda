package com.barda.sdk.test;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.annotations.VisibleForTesting;
import com.jayway.jsonpath.JsonPath;
import com.barda.sdk.util.JsonUtils;

/**
 * JSON 文件读取器类。
 * 该类提供了一组静态方法来读取 JSON 文件并返回所需的 Java 对象。
 */
@SuppressWarnings({"unused"})
@VisibleForTesting
public class JsonFileReader {

    /**
     * 从指定类中读取 JSON 文件并返回指定 JSONPath 所对应的 Java 对象。
     *
     * @param clazz  要读取 JSON 文件的类。
     * @param jsonPath  JSONPath 表达式。
     * @param <T>  要返回的 Java 对象的类型。
     * @return 读取的 Java 对象。
     */
    public static <T> T read(Class<?> clazz, String jsonPath) {
        String path = buildPath(clazz);
        try {
            String json = IOUtils.toString(new FileReader(path));
            return JsonPath.read(json, jsonPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从指定路径读取 JSON 文件并返回指定 JSONPath 所对应的 Java 对象。
     *
     * @param classpath  JSON 文件的路径。
     * @param jsonPath  JSONPath 表达式。
     * @param <T>  要返回的 Java 对象的类型。
     * @return 读取的 Java 对象。
     */
    public static <T> T read(String classpath, String jsonPath) {
        String path = buildPath(classpath);
        try {
            String json = IOUtils.toString(new FileReader(path));
            return JsonPath.read(json, jsonPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从指定类中读取 JSON 文件并返回一个包含所有对象的列表。
     *
     * @param classpath  要读取 JSON 文件的类。
     * @return 包含所有对象的列表。
     */
    public static List<Object> readList(String classpath) {
        String path = buildPath(classpath);
        try {
            String json = IOUtils.toString(new FileReader(path));
            return JsonUtils.fromJsonList(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从指定类中读取 JSON 文件并返回一个包含所有键值对的 Map。
     *
     * @param clazz  要读取 JSON 文件的类。
     * @return 包含所有键值对的 Map。
     */
    public static Map<String, Object> readMap(Class<?> clazz) {
        String path = buildPath(clazz);
        try {
            String json = IOUtils.toString(new FileReader(path));
            return JsonUtils.fromJsonMap(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从指定路径读取 JSON 文件并返回一个包含所有键值对的 Map。
     *
     * @param classpath  JSON 文件的路径。
     * @return 包含所有键值对的 Map。
     */
    public static Map<String, Object> readMap(String classpath) {
        String path = buildPath(classpath);
        try {
            String json = IOUtils.toString(new FileReader(path));
            return JsonUtils.fromJsonMap(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建 JSON 文件的路径。
     *
     * @param classpath  JSON 文件的路径。
     * @return 构建好的路径。
     */
    public static String buildPath(String classpath) {
        return String.format("./src/test/java/%s", classpath);
    }

    /**
     * 构建 JSON 文件的路径。
     *
     * @param clazz  要读取 JSON 文件的类。
     * @return 构建好的路径。
     */
    private static String buildPath(Class<?> clazz) {
        String path = clazz.getCanonicalName().replace(".", "/");
        return buildPath(path + ".json");
    }
}

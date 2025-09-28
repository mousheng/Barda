package com.barda.sdk.plugin.common;

/**
 * 该类提供了一组有用的SQL查询实用方法。
 */
public class SqlQueryUtils {

    /**
     * 检查查询是否是插入语句。
     *
     * @param query 查询语句
     * @return 如果是插入语句则返回true，否则返回false
     */
    public static boolean isInsertQuery(String query) {
        String[] queries = query.split(";");
        return queries[queries.length - 1].trim()
                .split("\\s+")[0]
                .equalsIgnoreCase("insert");
    }

    /**
     * 移除查询语句中的注释。
     *
     * @param query 查询语句
     * @return 移除注释后的查询语句
     */
    public static String removeQueryComments(String query) {
        StringBuilder sb = new StringBuilder();
        final int length = query.length();
        int commentCharacterCount = 0;
        boolean inComment = false;
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        for (int i = 0; i < length; i++) {
            char current = query.charAt(i);
            if ('\'' == current) {
                inSingleQuotes = !inSingleQuotes;
            }
            if ('\n' == current) {
                inComment = false;
            }
            if ('"' == current) {
                inDoubleQuotes = !inDoubleQuotes;
            }
            if ('-' == current) {
                if (!inDoubleQuotes && !inSingleQuotes) {
                    commentCharacterCount++;
                }
            } else if (!inComment) {
                commentCharacterCount = 0;
            }
            if (commentCharacterCount == 2) {
                inComment = true;
                sb.deleteCharAt(sb.length() - 1);
                commentCharacterCount = 0;
            }
            if (!inComment) {
                sb.append(current);
            }
        }
        return sb.toString().trim();
    }
}
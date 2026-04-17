package com.gk.study.common;

import java.util.Objects;

/**
 * 缓存 Key 工具类。
 * 用于生成商品列表缓存的 key，保证相同查询参数的请求命中同一个缓存。
 */
public class CacheKeyUtils {

    private CacheKeyUtils() {
    }

    /**
     * 根据查询参数构建缓存 key。
     * 保证 keyword/sort/c/tag 相同的请求生成相同的 key。
     */
    public static String buildKey(String keyword, String sort, String c, String tag) {
        String k = Objects.toString(keyword, "");
        String s = Objects.toString(sort, "");
        String cVal = Objects.toString(c, "");
        String t = Objects.toString(tag, "");
        return "kw=" + k + "|sort=" + s + "|c=" + cVal + "|tag=" + t;
    }
}

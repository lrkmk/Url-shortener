package com.kumiko.shorturl.util;


public class ShardRouter {
    public static String getTableName(String shortCode) {
        int hash = Math.abs(shortCode.hashCode()) % 4;
        return "url_mapping_" + String.valueOf(hash);
    }
}

package com.kumiko.shorturl.service;

public class Base62Encoder {
    private static final String CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long id) {
        StringBuilder result = new StringBuilder();
        while (id != 0) {
            long idx = id % 62;
            id /= 62;
            result.append(CHARSET.charAt((int) idx));
        }
        while (result.length() < 7) {
            result.append(CHARSET.charAt(0));
        }
        return result.reverse().toString();
    }
}

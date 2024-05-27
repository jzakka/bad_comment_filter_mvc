package com.example.bad_comment_filter_mvc.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CacheKey {
    private static final String ALGORITHM = "SHA-256";
    public static String getKeyFromText(String text) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No such algorithm:" + ALGORITHM, e);
        }
        md.update(text.getBytes());

        return bytesToHex(md.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}

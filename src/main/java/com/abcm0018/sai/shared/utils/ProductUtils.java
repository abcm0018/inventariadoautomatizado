package com.abcm0018.sai.shared.utils;

public class ProductUtils {
    private static final String EAN_REGEX = "^[0-9]{14}$";

    private ProductUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isEANValid(String ean) {
        return ean.matches(EAN_REGEX);
    }
}

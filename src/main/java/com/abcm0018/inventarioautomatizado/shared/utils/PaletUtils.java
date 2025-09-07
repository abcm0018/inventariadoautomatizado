package com.abcm0018.inventarioautomatizado.shared.utils;


public class PaletUtils {
    private static final String SSCC_REGEX = "^(?:0|[1-9]\\d{0,2})(?:\\.\\d{1,3})?$";
    private static final String EAN_REGEX = "^[0-9]{14}$";

    private PaletUtils(){throw new IllegalStateException("Utility class");}


    public static boolean isSSCCValid(String sscc) {
        return SSCC_REGEX.matches(sscc);
    }
    public static boolean isEANValid(String ean) {
        return EAN_REGEX.matches(ean);
    }

}

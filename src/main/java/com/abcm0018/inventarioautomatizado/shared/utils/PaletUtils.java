package com.abcm0018.inventarioautomatizado.shared.utils;


public class PaletUtils {
    private static final String SSCC_REGEX =  "^\\d{18}$";
    private static final String EAN_REGEX = "^[0-9]{14}$";

    private PaletUtils(){throw new IllegalStateException("Utility class");}

    public static boolean isSSCCValid(String sscc) {
        return sscc != null && sscc.matches(SSCC_REGEX);
    }
    public static boolean isEANValid(String ean) {
        return ean != null && ean.matches(EAN_REGEX);
    }

}

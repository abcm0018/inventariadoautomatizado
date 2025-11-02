package com.abcm0018.sai.shared.utils;


public class PaletUtils {
    private static final String SSCC_REGEX =  "^\\d{18}$";
    private static final String EAN_REGEX = "^[0-9]{14}$";
    private static final String BATCH_REGEX = "^[0-9]{7}[A-Z]$";
    private static final String SHIFT_REGEX = "^(MORNING|AFTERNOON|NIGHT)$";

    private PaletUtils(){throw new IllegalStateException("Utility class");}

    public static boolean isSSCCValid(String sscc) {
        return sscc.matches(SSCC_REGEX);
    }
    public static boolean isEANValid(String ean) {
        return ean.matches(EAN_REGEX);
    }

    public static boolean isBatchValid(String batch) {
        return batch.matches(BATCH_REGEX);
    }

    public static boolean isShiftValid(String shift) {

        return shift.toUpperCase().matches(SHIFT_REGEX);
    }

}

package com.abcm0018.inventarioautomatizado.shared.utils;

public class ShiftUtils {
    private static final String SHIFT_REGEX = "^(MORNING|AFTERNOON|NIGHT)$";

    private ShiftUtils(){throw new IllegalStateException("Utility class");}

    public static boolean isShiftValid(String shift) {
        return shift.toUpperCase().matches(SHIFT_REGEX);
    }
}

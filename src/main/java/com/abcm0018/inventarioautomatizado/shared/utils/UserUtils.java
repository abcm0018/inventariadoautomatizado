package com.abcm0018.inventarioautomatizado.shared.utils;

public class UserUtils {
    private static final String EMPLOYEE_NUMBER_REGEX =  "^[a-zA-Z][a-zA-Z0-9_]{2,19}$";

    private UserUtils(){throw new IllegalStateException("Utility class");}

    public static boolean isEmployeeNumberValid(String employeeNumber) {
        return employeeNumber.matches(EMPLOYEE_NUMBER_REGEX);
    }
}

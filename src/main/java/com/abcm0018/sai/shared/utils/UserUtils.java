package com.abcm0018.sai.shared.utils;

public class UserUtils {
    private static final String EMPLOYEE_NUMBER_REGEX =  "^[0-9]{8}[A-Z]$" ;

    private UserUtils(){throw new IllegalStateException("Utility class");}

    public static boolean isEmployeeNumberValid(String employeeNumber) {
        return employeeNumber.matches(EMPLOYEE_NUMBER_REGEX);
    }
}

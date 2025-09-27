package com.abcm0018.inventarioautomatizado.shared.utils;

public class WorkshiftUtils {
    private static final String EMPLOYEE_NUMBER_REGEX =  "^[0-9]{8}[A-Z]$" ;

    private WorkshiftUtils(){throw new IllegalStateException("Utility class");}

    public static boolean isEmployeeNumberValid(String employeeNumber) {
        return employeeNumber.matches(EMPLOYEE_NUMBER_REGEX);
    }
}

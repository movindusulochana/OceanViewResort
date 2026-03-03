package com.oceanview.resort.util;

import java.util.regex.Pattern;
import java.time.LocalDate;

/**
 * Task B: Validation Mechanism.
 * This helper class provides static methods to validate user inputs 
 * from the JavaFX UI before processing them in the Service or DAO layers.
 */
public class ValidationHelper {

    // Regular expression for a standard 10-digit phone number
    private static final String PHONE_REGEX = "^[0-9]{10}$";
    
    // Simple email regex for guest registration
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /**
     * Checks if a string is null or empty.
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Validates that a phone number contains exactly 10 digits.
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        return Pattern.matches(PHONE_REGEX, phone);
    }

    /**
     * Validates email format.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return Pattern.matches(EMAIL_REGEX, email);
    }

    /**
     * Ensures the check-out date is actually after the check-in date.
     */
    public static boolean isValidStayDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return false;
        return checkOut.isAfter(checkIn);
    }

    /**
     * Validates that a string can be safely converted to a numeric value (e.g., for rates).
     */
    public static boolean isNumeric(String text) {
        if (isEmpty(text)) return false;
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
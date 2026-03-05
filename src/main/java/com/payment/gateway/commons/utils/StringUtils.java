package com.payment.gateway.commons.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for string operations.
 */
public final class StringUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[1-9]\\d{1,14}$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]+$"
    );

    private StringUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Check if a string is null or blank.
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not blank.
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Check if a string is a valid email address.
     */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    /**
     * Check if a string is a valid phone number (E.164 format).
     */
    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        Matcher matcher = PHONE_PATTERN.matcher(phone);
        return matcher.matches();
    }

    /**
     * Check if a string is alphanumeric.
     */
    public static boolean isAlphanumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        Matcher matcher = ALPHANUMERIC_PATTERN.matcher(str);
        return matcher.matches();
    }

    /**
     * Truncate a string to a maximum length.
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }

    /**
     * Generate a masked version of a string (for logging sensitive data).
     */
    public static String mask(String str, int visibleAtStart, int visibleAtEnd) {
        if (isBlank(str)) {
            return str;
        }
        int length = str.length();
        if (length <= visibleAtStart + visibleAtEnd) {
            return "*".repeat(length);
        }
        return str.substring(0, visibleAtStart) +
               "*".repeat(length - visibleAtStart - visibleAtEnd) +
               str.substring(length - visibleAtEnd);
    }

    /**
     * Mask a card number, showing only last 4 digits.
     */
    public static String maskCardNumber(String cardNumber) {
        if (isBlank(cardNumber)) {
            return cardNumber;
        }
        String cleaned = cardNumber.replaceAll("\\s", "");
        if (cleaned.length() <= 4) {
            return "*".repeat(cleaned.length());
        }
        return "*".repeat(cleaned.length() - 4) + cleaned.substring(cleaned.length() - 4);
    }

    /**
     * Normalize a string by trimming and converting to lowercase.
     */
    public static String normalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.trim().toLowerCase();
    }

    /**
     * Capitalize the first letter of a string.
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }

    /**
     * Generate initials from a full name.
     */
    public static String getInitials(String fullName) {
        if (isBlank(fullName)) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return initials.toString();
    }
}

package com.payment.gateway.commons.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringUtils.
 */
class StringUtilsTest {

    @Test
    void shouldDetectBlankStrings() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertTrue(StringUtils.isBlank("\t\n"));
    }

    @Test
    void shouldDetectNonBlankStrings() {
        assertTrue(StringUtils.isNotBlank("test"));
        assertTrue(StringUtils.isNotBlank(" test "));
    }

    @Test
    void shouldValidateEmail() {
        assertTrue(StringUtils.isValidEmail("test@example.com"));
        assertTrue(StringUtils.isValidEmail("user.name+tag@domain.co.uk"));
        assertTrue(StringUtils.isValidEmail("user@subdomain.example.org"));
    }

    @Test
    void shouldRejectInvalidEmails() {
        assertFalse(StringUtils.isValidEmail(null));
        assertFalse(StringUtils.isValidEmail(""));
        assertFalse(StringUtils.isValidEmail("invalid"));
        assertFalse(StringUtils.isValidEmail("invalid@"));
        assertFalse(StringUtils.isValidEmail("@example.com"));
        assertFalse(StringUtils.isValidEmail("user@.com"));
    }

    @Test
    void shouldValidatePhone() {
        assertTrue(StringUtils.isValidPhone("+1234567890"));
        assertTrue(StringUtils.isValidPhone("+5511999999999"));
        assertTrue(StringUtils.isValidPhone("1234567890"));
    }

    @Test
    void shouldRejectInvalidPhones() {
        assertFalse(StringUtils.isValidPhone(null));
        assertFalse(StringUtils.isValidPhone(""));
        assertFalse(StringUtils.isValidPhone("abc123"));
        assertFalse(StringUtils.isValidPhone("+0123456789"));
    }

    @Test
    void shouldValidateAlphanumeric() {
        assertTrue(StringUtils.isAlphanumeric("abc123"));
        assertTrue(StringUtils.isAlphanumeric("ABC"));
        assertTrue(StringUtils.isAlphanumeric("123"));
    }

    @Test
    void shouldRejectNonAlphanumeric() {
        assertFalse(StringUtils.isAlphanumeric(null));
        assertFalse(StringUtils.isAlphanumeric(""));
        assertFalse(StringUtils.isAlphanumeric("abc-123"));
        assertFalse(StringUtils.isAlphanumeric("abc_123"));
        assertFalse(StringUtils.isAlphanumeric("abc 123"));
    }

    @Test
    void shouldTruncateString() {
        assertEquals("hello", StringUtils.truncate("hello world", 5));
        assertEquals("hello", StringUtils.truncate("hello", 5));
        assertEquals("hello", StringUtils.truncate("hello", 10));
    }

    @Test
    void shouldHandleNullTruncate() {
        assertNull(StringUtils.truncate(null, 5));
    }

    @Test
    void shouldMaskString() {
        assertEquals("****", StringUtils.mask("test", 0, 0));
        assertEquals("t**t", StringUtils.mask("test", 1, 1));
        assertEquals("te**", StringUtils.mask("test", 2, 0));
    }

    @Test
    void shouldHandleShortStringMask() {
        // When string length <= visibleAtStart + visibleAtEnd, all chars are masked
        assertEquals("***", StringUtils.mask("abc", 2, 2));
        assertEquals("**", StringUtils.mask("ab", 1, 1));
        assertEquals("*", StringUtils.mask("a", 1, 1));
        // When string length > visibleAtStart + visibleAtEnd, shows first and last chars
        assertEquals("a*c", StringUtils.mask("abc", 1, 1));
    }

    @Test
    void shouldMaskCardNumber() {
        assertEquals("************1234", StringUtils.maskCardNumber("1234567890121234"));
        assertEquals("************1234", StringUtils.maskCardNumber("1234 5678 9012 1234"));
        assertEquals("****", StringUtils.maskCardNumber("1234"));
        assertEquals("***", StringUtils.maskCardNumber("123"));
    }

    @Test
    void shouldHandleNullMaskCardNumber() {
        assertNull(StringUtils.maskCardNumber(null));
    }

    @Test
    void shouldNormalizeString() {
        assertEquals("test", StringUtils.normalize("  TEST  "));
        assertEquals("test", StringUtils.normalize("test"));
        assertNull(StringUtils.normalize(null));
    }

    @Test
    void shouldCapitalize() {
        assertEquals("Test", StringUtils.capitalize("test"));
        assertEquals("Test", StringUtils.capitalize("TEST"));
        assertEquals("Test", StringUtils.capitalize("test"));
        assertNull(StringUtils.capitalize(null));
    }

    @Test
    void shouldGetInitials() {
        assertEquals("JD", StringUtils.getInitials("John Doe"));
        assertEquals("JDK", StringUtils.getInitials("John Doe Kim"));
        assertEquals("J", StringUtils.getInitials("John"));
        assertEquals("", StringUtils.getInitials(""));
        assertEquals("", StringUtils.getInitials(null));
    }
}

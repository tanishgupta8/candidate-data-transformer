package com.eightfold.normalizer;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public final class PhoneNormalizerTest {

    @Test
    public void testPhoneNormalizationSuccess() {
        // US phone formats
        Optional<String> usPhone1 = PhoneNormalizer.INSTANCE.normalize("+1 (415) 555-2671");
        Optional<String> usPhone2 = PhoneNormalizer.INSTANCE.normalize("415 555 2671");
        Optional<String> usPhone3 = PhoneNormalizer.INSTANCE.normalize("415-555-2671");

        assertTrue(usPhone1.isPresent());
        assertEquals("+14155552671", usPhone1.get());

        assertTrue(usPhone2.isPresent());
        assertEquals("+14155552671", usPhone2.get());

        assertTrue(usPhone3.isPresent());
        assertEquals("+14155552671", usPhone3.get());
    }

    @Test
    public void testInternationalPhoneNormalization() {
        // India phone format
        Optional<String> inPhone = PhoneNormalizer.INSTANCE.normalize("+91 98765 43210");
        assertTrue(inPhone.isPresent());
        assertEquals("+919876543210", inPhone.get());
    }

    @Test
    public void testInvalidPhoneNumbers() {
        // Malformed or invalid numbers
        Optional<String> invalid1 = PhoneNormalizer.INSTANCE.normalize("not-a-number");
        Optional<String> invalid2 = PhoneNormalizer.INSTANCE.normalize("123");
        Optional<String> invalid3 = PhoneNormalizer.INSTANCE.normalize("");
        Optional<String> invalid4 = PhoneNormalizer.INSTANCE.normalize(null);

        assertFalse(invalid1.isPresent());
        assertFalse(invalid2.isPresent());
        assertFalse(invalid3.isPresent());
        assertFalse(invalid4.isPresent());
    }
}

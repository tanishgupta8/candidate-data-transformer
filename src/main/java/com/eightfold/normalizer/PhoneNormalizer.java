package com.eightfold.normalizer;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Normalizes phone numbers to E.164 format using Google libphonenumber.
 */
public final class PhoneNormalizer implements NormalizerStrategy<String> {
    private static final Logger logger = LoggerFactory.getLogger(PhoneNormalizer.class);
    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private static final String DEFAULT_REGION = "US";

    public static final PhoneNormalizer INSTANCE = new PhoneNormalizer();

    private PhoneNormalizer() {}

    @Override
    public Optional<String> normalize(String phoneStr) {
        if (phoneStr == null || phoneStr.isBlank()) {
            return Optional.empty();
        }

        try {
            String cleaned = phoneStr.trim();
            PhoneNumber number = phoneUtil.parse(cleaned, DEFAULT_REGION);
            if (phoneUtil.isValidNumber(number)) {
                return Optional.of(phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164));
            } else {
                logger.debug("Phone number parsed but is invalid: {}", phoneStr);
            }
        } catch (NumberParseException e) {
            logger.debug("Failed to parse phone number: {} (Reason: {})", phoneStr, e.getMessage());
        }

        return Optional.empty();
    }
}

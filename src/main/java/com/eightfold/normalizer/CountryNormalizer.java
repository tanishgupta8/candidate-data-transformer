package com.eightfold.normalizer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Normalizes country names and codes to ISO-3166 alpha-2 format.
 */
public final class CountryNormalizer implements NormalizerStrategy<String> {
    private static final Map<String, String> COUNTRY_MAP = new HashMap<>();

    public static final CountryNormalizer INSTANCE = new CountryNormalizer();

    static {
        // Build map from standard JDK locales
        for (String countryCode : Locale.getISOCountries()) {
            Locale locale = new Locale("", countryCode);
            String englishName = locale.getDisplayCountry(Locale.ENGLISH).toLowerCase();
            String iso3 = locale.getISO3Country().toLowerCase();

            COUNTRY_MAP.put(englishName, countryCode);
            COUNTRY_MAP.put(iso3, countryCode);
            COUNTRY_MAP.put(countryCode.toLowerCase(), countryCode);
        }

        // Add common variations/aliases
        COUNTRY_MAP.put("united states of america", "US");
        COUNTRY_MAP.put("usa", "US");
        COUNTRY_MAP.put("united kingdom", "GB");
        COUNTRY_MAP.put("uk", "GB");
        COUNTRY_MAP.put("great britain", "GB");
        COUNTRY_MAP.put("india", "IN");
        COUNTRY_MAP.put("deutschland", "DE");
        COUNTRY_MAP.put("germany", "DE");
        COUNTRY_MAP.put("federated states of micronesia", "FM");
    }

    private CountryNormalizer() {}

    @Override
    public Optional<String> normalize(String country) {
        if (country == null || country.isBlank()) {
            return Optional.empty();
        }

        String cleaned = country.trim().toLowerCase();

        // 1. Exact match in the country map
        String code = COUNTRY_MAP.get(cleaned);
        if (code != null) {
            return Optional.of(code);
        }

        // 2. Substring match or partial match fallback
        for (Map.Entry<String, String> entry : COUNTRY_MAP.entrySet()) {
            if (cleaned.endsWith(" " + entry.getKey()) || cleaned.endsWith(", " + entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }

        return Optional.empty();
    }
}

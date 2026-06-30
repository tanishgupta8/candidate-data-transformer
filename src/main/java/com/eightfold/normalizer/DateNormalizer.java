package com.eightfold.normalizer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes dates from various formats into YYYY-MM format.
 */
public final class DateNormalizer implements NormalizerStrategy<String> {
    private static final List<DateTimeFormatter> FORMATTERS = new ArrayList<>();
    private static final Pattern YEAR_ONLY_PATTERN = Pattern.compile("^\\b(19|20)\\d{2}\\b$");

    public static final DateNormalizer INSTANCE = new DateNormalizer();

    static {
        // Add various formatters to try parsing
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-M-d"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/M/d"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("M/d/yyyy"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        FORMATTERS.add(DateTimeFormatter.ofPattern("d-M-yyyy"));

        // Month-Year formatters
        FORMATTERS.add(new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH));
        FORMATTERS.add(new DateTimeFormatterBuilder()
                .appendPattern("MM/yyyy")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH));
        FORMATTERS.add(new DateTimeFormatterBuilder()
                .appendPattern("M/yyyy")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH));
        FORMATTERS.add(new DateTimeFormatterBuilder()
                .appendPattern("MM-yyyy")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH));
        FORMATTERS.add(new DateTimeFormatterBuilder()
                .appendPattern("MMM yyyy")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH));
        FORMATTERS.add(new DateTimeFormatterBuilder()
                .appendPattern("MMMM yyyy")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH));
        FORMATTERS.add(new DateTimeFormatterBuilder()
                .appendPattern("MMM-yyyy")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH));
        FORMATTERS.add(new DateTimeFormatterBuilder()
                .appendPattern("MMMM-yyyy")
                .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                .toFormatter(Locale.ENGLISH));
    }

    private DateNormalizer() {}

    @Override
    public Optional<String> normalize(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return Optional.empty();
        }

        String cleaned = dateStr.trim();

        // Handle "Present" or "Current"
        if (cleaned.equalsIgnoreCase("present") || cleaned.equalsIgnoreCase("current") || cleaned.equalsIgnoreCase("now")) {
            return Optional.of("Present");
        }

        // Handle Year only e.g. "2021" -> "2021-01"
        Matcher yearMatcher = YEAR_ONLY_PATTERN.matcher(cleaned);
        if (yearMatcher.matches()) {
            return Optional.of(cleaned + "-01");
        }

        // Try parsing using registered formatters
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(cleaned, formatter);
                return Optional.of(String.format("%04d-%02d", date.getYear(), date.getMonthValue()));
            } catch (DateTimeParseException ignored) {
            }
        }

        // Attempt regex fallback if dates are slightly malformed
        cleaned = cleaned.replace(",", " ").replace(".", "/");
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(cleaned, formatter);
                return Optional.of(String.format("%04d-%02d", date.getYear(), date.getMonthValue()));
            } catch (DateTimeParseException ignored) {
            }
        }

        return Optional.empty();
    }
}

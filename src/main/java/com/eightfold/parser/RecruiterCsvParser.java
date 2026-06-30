package com.eightfold.parser;

import com.eightfold.exception.ParseException;
import com.eightfold.model.CandidateProfile;
import com.eightfold.model.Experience;
import com.eightfold.model.Provenance;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses candidate data from structured Recruiter CSV files.
 */
public final class RecruiterCsvParser implements SourceParser {
    private static final Logger logger = LoggerFactory.getLogger(RecruiterCsvParser.class);
    private static final String SOURCE_NAME = "Recruiter CSV";

    @Override
    public CandidateProfile parse(File file) throws IOException {
        logger.info("Parsing CSV file: {}", file.getAbsolutePath());
        CandidateProfile.Builder builder = CandidateProfile.builder();

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                logger.warn("CSV file is empty: {}", file.getName());
                return builder.build();
            }

            String[] headers = rows.get(0);
            int nameIdx = -1;
            int emailIdx = -1;
            int phoneIdx = -1;
            int companyIdx = -1;
            int titleIdx = -1;

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim().toLowerCase();
                switch (h) {
                    case "name" -> nameIdx = i;
                    case "email" -> emailIdx = i;
                    case "phone" -> phoneIdx = i;
                    case "current_company", "company" -> companyIdx = i;
                    case "title", "headline" -> titleIdx = i;
                }
            }

            if (rows.size() > 1) {
                // Parse the first candidate record row
                String[] data = rows.get(1);
                String name = getValue(data, nameIdx);
                String email = getValue(data, emailIdx);
                String phone = getValue(data, phoneIdx);
                String company = getValue(data, companyIdx);
                String title = getValue(data, titleIdx);

                if (name != null) {
                    builder.fullName(name);
                    builder.putProvenance("fullName", new Provenance("fullName", SOURCE_NAME, "parse"));
                }
                if (email != null) {
                    builder.addEmail(email);
                    builder.putProvenance("emails", new Provenance("emails", SOURCE_NAME, "parse"));
                }
                if (phone != null) {
                    builder.addPhone(phone);
                    builder.putProvenance("phones", new Provenance("phones", SOURCE_NAME, "parse"));
                }
                if (title != null) {
                    builder.headline(title);
                    builder.putProvenance("headline", new Provenance("headline", SOURCE_NAME, "parse"));
                }

                // If we have both company and title, map it to an Experience entry
                if (company != null || title != null) {
                    Experience exp = new Experience(
                            title != null ? title : "",
                            company != null ? company : "",
                            "",   // Raw CSV does not have start/end dates
                            "",
                            "Current role from Recruiter profile"
                    );
                    builder.addExperience(exp);
                    builder.putProvenance("experience", new Provenance("experience", SOURCE_NAME, "parse"));
                }
            } else {
                logger.warn("CSV has headers but no data rows.");
            }
        } catch (CsvException e) {
            logger.error("Error reading CSV content: {}", e.getMessage());
            throw new ParseException("Malformed CSV content in file: " + file.getName(), e);
        } catch (IOException e) {
            logger.error("I/O failure reading CSV file: {}", e.getMessage());
            throw new ParseException("I/O failure reading CSV file: " + file.getName(), e);
        }

        return builder.build();
    }

    private String getValue(String[] row, int index) {
        if (index >= 0 && index < row.length) {
            String val = row[index];
            return val != null && !val.isBlank() ? val.trim() : null;
        }
        return null;
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }
}

package com.eightfold.parser;

import com.eightfold.exception.ParseException;
import com.eightfold.model.CandidateProfile;
import com.eightfold.model.Education;
import com.eightfold.model.Experience;
import com.eightfold.model.Provenance;
import com.eightfold.model.Skill;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses unstructured candidate resumes using Apache PDFBox.
 */
public final class ResumePdfParser implements SourceParser {
    private static final Logger logger = LoggerFactory.getLogger(ResumePdfParser.class);
    private static final String SOURCE_NAME = "Resume PDF";

    // Regex patterns for entity extraction
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,4}[\\s.-]?)?\\(?\\d{2,4}\\)?[\\s.-]?\\d{3,4}[\\s.-]?\\d{4}");
    private static final Pattern DATE_RANGE_PATTERN = Pattern.compile(
            "(\\b\\d{4}[-/]\\d{2}\\b|\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{4}\\b|\\b\\d{4}\\b)\\s+(to|-|until)\\s+(\\b\\d{4}[-/]\\d{2}\\b|\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+\\d{4}\\b|\\b\\d{4}\\b|Present|Current|Now)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public CandidateProfile parse(File file) throws IOException {
        logger.info("Parsing PDF resume file: {}", file.getAbsolutePath());
        CandidateProfile.Builder builder = CandidateProfile.builder();

        String text;
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        } catch (Exception e) {
            logger.error("Failed to load/parse PDF document {}: {}", file.getName(), e.getMessage());
            throw new ParseException("Failed to load or parse PDF document: " + file.getName(), e);
        }

        if (text == null || text.isBlank()) {
            logger.warn("PDF resume text content is empty.");
            return builder.build();
        }

        // 1. Extract contact information using regex
        extractEmails(text, builder);
        extractPhones(text, builder);
        extractLinks(text, builder);
        extractLocation(text, builder);

        // 2. Extract full name (first non-empty line without contact info)
        extractFullName(text, builder);

        // 3. Section-based stateful parsing
        parseSections(text, builder);

        return builder.build();
    }

    private void extractLocation(String text, CandidateProfile.Builder builder) {
        Pattern locationPattern = Pattern.compile("Location:\\s*([^|\\r\\n]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = locationPattern.matcher(text);
        if (matcher.find()) {
            builder.location(matcher.group(1).trim());
            builder.putProvenance("location", new Provenance("location", SOURCE_NAME, "regex"));
        }
    }

    private void extractEmails(String text, CandidateProfile.Builder builder) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        boolean found = false;
        while (matcher.find()) {
            builder.addEmail(matcher.group());
            found = true;
        }
        if (found) {
            builder.putProvenance("emails", new Provenance("emails", SOURCE_NAME, "regex"));
        }
    }

    private void extractPhones(String text, CandidateProfile.Builder builder) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        boolean found = false;
        while (matcher.find()) {
            builder.addPhone(matcher.group());
            found = true;
        }
        if (found) {
            builder.putProvenance("phones", new Provenance("phones", SOURCE_NAME, "regex"));
        }
    }

    private void extractLinks(String text, CandidateProfile.Builder builder) {
        Pattern linkPattern = Pattern.compile("(https?://)?(www\\.)?(github\\.com|linkedin\\.com)/[a-zA-Z0-9_.-]+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = linkPattern.matcher(text);
        boolean found = false;
        while (matcher.find()) {
            builder.addLink(matcher.group());
            found = true;
        }
        if (found) {
            builder.putProvenance("links", new Provenance("links", SOURCE_NAME, "regex"));
        }
    }

    private void extractFullName(String text, CandidateProfile.Builder builder) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            // Simple heuristics to ignore header labels or emails/phones in first line
            String lower = trimmed.toLowerCase();
            if (lower.contains("email") || lower.contains("@") || lower.contains("phone") || lower.contains("resume") || lower.contains("cv")) {
                continue;
            }
            builder.fullName(trimmed);
            builder.putProvenance("fullName", new Provenance("fullName", SOURCE_NAME, "heuristic"));
            break;
        }
    }

    private void parseSections(String text, CandidateProfile.Builder builder) {
        String[] lines = text.split("\\r?\\n");
        String currentSection = "";
        List<String> sectionLines = new ArrayList<>();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            String lower = trimmed.toLowerCase();
            String newSection = null;

            if (lower.startsWith("experience") || lower.startsWith("work experience") || lower.startsWith("employment")) {
                newSection = "experience";
            } else if (lower.startsWith("education") || lower.startsWith("academic")) {
                newSection = "education";
            } else if (lower.startsWith("skills") || lower.startsWith("technical skills") || lower.startsWith("technologies")) {
                newSection = "skills";
            } else if (trimmed.matches("^[A-Z][a-zA-Z\\s]{2,15}:$")) {
                newSection = "other";
            }

            if (newSection != null) {
                // Process accumulated lines of previous section
                processSectionData(currentSection, sectionLines, builder);
                currentSection = newSection;
                sectionLines.clear();
            } else if (!currentSection.isEmpty()) {
                sectionLines.add(trimmed);
            }
        }
        // Process final section
        processSectionData(currentSection, sectionLines, builder);
    }

    private void processSectionData(String section, List<String> lines, CandidateProfile.Builder builder) {
        if (lines.isEmpty()) return;

        switch (section) {
            case "skills" -> {
                for (String line : lines) {
                    String[] parts = line.split("[,;|\\t]");
                    for (String part : parts) {
                        String skillName = part.trim();
                        if (!skillName.isEmpty()) {
                            builder.addSkill(new Skill(skillName));
                        }
                    }
                }
                builder.putProvenance("skills", new Provenance("skills", SOURCE_NAME, "section_parse"));
            }
            case "experience" -> {
                parseExperiences(lines, builder);
            }
            case "education" -> {
                parseEducations(lines, builder);
            }
        }
    }

    private void parseExperiences(List<String> lines, CandidateProfile.Builder builder) {
        String currentCompanyAndTitle = null;
        String currentDates = null;
        StringBuilder currentDesc = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            Matcher dateMatcher = DATE_RANGE_PATTERN.matcher(line);

            if (dateMatcher.find()) {
                // If we already had an experience being parsed, build it
                if (currentCompanyAndTitle != null) {
                    addExperienceToBuilder(currentCompanyAndTitle, currentDates, currentDesc.toString(), builder);
                    currentDesc.setLength(0);
                }
                currentDates = dateMatcher.group();

                // Guess company and title from preceding lines or same line
                if (i > 0) {
                    currentCompanyAndTitle = lines.get(i - 1);
                } else {
                    currentCompanyAndTitle = line.replace(currentDates, "").trim();
                }
            } else {
                if (currentCompanyAndTitle != null) {
                    // Check if this line is a new experience header (without date) or just description
                    // Let's assume description for lines after dates
                    if (currentDesc.length() > 0) {
                        currentDesc.append(" ");
                    }
                    currentDesc.append(line);
                }
            }
        }

        // Add last experience
        if (currentCompanyAndTitle != null) {
            addExperienceToBuilder(currentCompanyAndTitle, currentDates, currentDesc.toString(), builder);
        }
    }

    private void addExperienceToBuilder(String compTitle, String dateRange, String desc, CandidateProfile.Builder builder) {
        String company = "";
        String title = "";
        if (compTitle != null) {
            String[] parts = compTitle.split("[-|,]");
            if (parts.length > 1) {
                company = parts[0].trim();
                title = parts[1].trim();
            } else {
                title = compTitle.trim();
            }
        }

        String startDate = "";
        String endDate = "";
        if (dateRange != null) {
            String[] dates = dateRange.split("(?i)\\s+(to|-|until)\\s+");
            if (dates.length > 0) startDate = dates[0].trim();
            if (dates.length > 1) endDate = dates[1].trim();
        }

        builder.addExperience(new Experience(title, company, startDate, endDate, desc));
        builder.putProvenance("experience", new Provenance("experience", SOURCE_NAME, "section_parse"));
    }

    private void parseEducations(List<String> lines, CandidateProfile.Builder builder) {
        for (String line : lines) {
            String degree = "";
            String institution = "";
            String fieldOfStudy = "";
            String startDate = "";
            String endDate = "";

            Matcher dateMatcher = DATE_RANGE_PATTERN.matcher(line);
            String dateRange = dateMatcher.find() ? dateMatcher.group() : "";

            String details = line.replace(dateRange, "").trim();
            String[] parts = details.split("[-|,]");

            if (parts.length > 0) {
                institution = parts[0].trim();
            }
            if (parts.length > 1) {
                String degreeDetails = parts[1].trim();
                if (degreeDetails.toLowerCase().contains("in")) {
                    String[] inParts = degreeDetails.split("(?i)\\s+in\\s+");
                    degree = inParts[0].trim();
                    if (inParts.length > 1) {
                        fieldOfStudy = inParts[1].trim();
                    }
                } else {
                    degree = degreeDetails;
                }
            }
            if (parts.length > 2 && fieldOfStudy.isEmpty()) {
                fieldOfStudy = parts[2].trim();
            }

            if (!dateRange.isEmpty()) {
                String[] dates = dateRange.split("(?i)\\s+(to|-|until)\\s+");
                if (dates.length > 0) startDate = dates[0].trim();
                if (dates.length > 1) endDate = dates[1].trim();
            }

            if (!institution.isEmpty() || !degree.isEmpty()) {
                builder.addEducation(new Education(degree, institution, fieldOfStudy, startDate, endDate));
                builder.putProvenance("education", new Provenance("education", SOURCE_NAME, "section_parse"));
            }
        }
    }

    @Override
    public String getSourceName() {
        return SOURCE_NAME;
    }
}

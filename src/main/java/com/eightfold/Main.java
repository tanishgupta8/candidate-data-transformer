package com.eightfold;

import com.eightfold.config.ConfigLoader;
import com.eightfold.confidence.ConfidenceEngine;
import com.eightfold.merger.ConflictResolver;
import com.eightfold.merger.MergeEngine;
import com.eightfold.merger.SourcePriorityConflictResolutionStrategy;
import com.eightfold.model.CandidateProfile;
import com.eightfold.normalizer.ProfileNormalizer;
import com.eightfold.parser.ParserFactory;
import com.eightfold.parser.SourceParser;
import com.eightfold.projection.ProjectionEngine;
import com.eightfold.util.Constants;
import com.eightfold.util.FileUtil;
import com.eightfold.util.JsonUtil;
import com.eightfold.validation.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CLI Entrypoint for the Multi-Source Candidate Data Transformer.
 */
public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String csvPath = null;
        String resumePath = null;
        String configPath = null;
        String outputPath = Constants.DEFAULT_OUTPUT_PATH;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--csv") && i + 1 < args.length) {
                csvPath = args[++i];
            } else if (args[i].equalsIgnoreCase("--resume") && i + 1 < args.length) {
                resumePath = args[++i];
            } else if (args[i].equalsIgnoreCase("--config") && i + 1 < args.length) {
                configPath = args[++i];
            } else if (args[i].equalsIgnoreCase("--output") && i + 1 < args.length) {
                outputPath = args[++i];
            }
        }

        try {
            executePipeline(csvPath, resumePath, configPath, outputPath);
        } catch (Exception e) {
            logger.error("Execution failed due to error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void executePipeline(final String csvPath, final String resumePath, final String configPath, final String outputPath) throws Exception {
        final long startTime = System.currentTimeMillis();
        final List<CandidateProfile> profiles = new ArrayList<>();

        // 1. Parsing & Normalizing Source 1 (CSV)
        if (csvPath != null) {
            logger.info("Loading CSV");
            final File csvFile = new File(csvPath);
            if (FileUtil.isValidFile(csvFile)) {
                final SourceParser parser = ParserFactory.getParser(csvFile);
                final CandidateProfile rawCsvProfile = parser.parse(csvFile);
                logger.info("Normalizing Data");
                final CandidateProfile normalizedCsvProfile = ProfileNormalizer.normalize(rawCsvProfile);
                profiles.add(normalizedCsvProfile);
            } else {
                logger.error("CSV file does not exist or is invalid: {}", csvPath);
            }
        }

        // 2. Parsing & Normalizing Source 2 (Resume PDF)
        if (resumePath != null) {
            logger.info("Loading Resume");
            final File resumeFile = new File(resumePath);
            if (FileUtil.isValidFile(resumeFile)) {
                final SourceParser parser = ParserFactory.getParser(resumeFile);
                final CandidateProfile rawResumeProfile = parser.parse(resumeFile);
                logger.info("Normalizing Data");
                final CandidateProfile normalizedResumeProfile = ProfileNormalizer.normalize(rawResumeProfile);
                profiles.add(normalizedResumeProfile);
            } else {
                logger.error("Resume PDF file does not exist or is invalid: {}", resumePath);
            }
        }

        if (profiles.isEmpty()) {
            logger.error("No valid candidate source profiles were successfully loaded. Exiting pipeline.");
            return;
        }

        // 3. Merging profiles
        logger.info("Generating Canonical Profile");
        logger.info("Resolving Conflicts");
        final ConflictResolver conflictResolver = new ConflictResolver(new SourcePriorityConflictResolutionStrategy(Constants.DEFAULT_SOURCE_PRIORITIES));
        final MergeEngine mergeEngine = new MergeEngine(conflictResolver);
        final CandidateProfile merged = mergeEngine.merge(profiles);

        // 4. Calculating confidence scores
        logger.info("Calculating Confidence");
        final ConfidenceEngine confidenceEngine = new ConfidenceEngine();
        final CandidateProfile canonicalProfile = confidenceEngine.calculateConfidence(merged, profiles);

        // 5. Loading projection configuration
        final Map<String, Object> config = ConfigLoader.loadConfig(configPath);

        // 6. Output projection
        logger.info("Applying Projection");
        final ProjectionEngine projectionEngine = new ProjectionEngine();
        final Map<String, Object> projectedOutput = projectionEngine.project(canonicalProfile, config);

        // 7. Output validation against schema
        String jsonOutput = JsonUtil.toJson(projectedOutput);
        logger.info("Validating JSON");
        try (final InputStream schemaStream = Main.class.getClassLoader().getResourceAsStream(Constants.DEFAULT_SCHEMA_PATH)) {
            if (schemaStream != null) {
                final SchemaValidator validator = new SchemaValidator(schemaStream);
                final boolean isValid = validator.validate(jsonOutput);
                if (isValid) {
                    logger.info("Validation successful");
                } else {
                    logger.warn("Generated candidate JSON does not conform to validation schema.");
                }
            } else {
                logger.warn("JSON Schema resource not found at '{}'. Skipping schema validation step.", Constants.DEFAULT_SCHEMA_PATH);
            }
        }

        // 8. Output Serialization
        logger.info("Writing Output");
        final File outputFile = new File(outputPath);
        FileUtil.writeString(outputFile, jsonOutput);

        logger.info("Completed Successfully");

        // Execution Summary Output
        final long endTime = System.currentTimeMillis();
        final long duration = endTime - startTime;

        final List<String> sources = new ArrayList<>();
        if (csvPath != null) {
            sources.add("Recruiter CSV");
        }
        if (resumePath != null) {
            sources.add("Resume PDF");
        }
        final String sourcesStr = sources.size() + " (" + String.join(", ", sources) + ")";

        final int fieldsExtracted = countExtractedFields(canonicalProfile);
        final int conflictsResolved = mergeEngine.getResolvedConflictsCount();
        final int skillsNormalized = canonicalProfile.getSkills() != null ? canonicalProfile.getSkills().size() : 0;
        final double confidence = canonicalProfile.getOverallConfidence() != null ? canonicalProfile.getOverallConfidence() : 0.0;

        System.out.println("----------------------------------");
        System.out.println("Candidate processed: " + (canonicalProfile.getFullName() != null ? canonicalProfile.getFullName() : "Unknown"));
        System.out.println("Sources processed: " + sourcesStr);
        System.out.println("Fields extracted: " + fieldsExtracted);
        System.out.println("Conflicts resolved: " + conflictsResolved);
        System.out.println("Skills normalized: " + skillsNormalized);
        System.out.println("Overall confidence: " + String.format("%.2f", confidence));
        System.out.println("Execution time: " + duration + " ms");
        System.out.println("Output path: " + outputPath);
        System.out.println("----------------------------------");
    }

    private static int countExtractedFields(final CandidateProfile profile) {
        int count = 0;
        if (profile.getFullName() != null && !profile.getFullName().isBlank()) count++;
        if (profile.getEmails() != null && !profile.getEmails().isEmpty()) count++;
        if (profile.getPhones() != null && !profile.getPhones().isEmpty()) count++;
        if (profile.getLocation() != null && !profile.getLocation().isBlank()) count++;
        if (profile.getHeadline() != null && !profile.getHeadline().isBlank()) count++;
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) count++;
        if (profile.getExperience() != null && !profile.getExperience().isEmpty()) count++;
        if (profile.getEducation() != null && !profile.getEducation().isEmpty()) count++;
        if (profile.getYearsExperience() != null && profile.getYearsExperience() > 0.0) count++;
        return count;
    }
}

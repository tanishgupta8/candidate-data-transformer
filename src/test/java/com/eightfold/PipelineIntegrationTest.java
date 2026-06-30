package com.eightfold;

import com.eightfold.confidence.ConfidenceEngine;
import com.eightfold.merger.ConflictResolver;
import com.eightfold.merger.MergeEngine;
import com.eightfold.merger.SourcePriorityConflictResolutionStrategy;
import com.eightfold.model.CandidateProfile;
import com.eightfold.normalizer.ProfileNormalizer;
import com.eightfold.parser.RecruiterCsvParser;
import com.eightfold.parser.ResumePdfParser;
import com.eightfold.projection.ProjectionEngine;
import com.eightfold.util.Constants;
import com.eightfold.util.SampleGenerator;
import com.eightfold.validation.SchemaValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public final class PipelineIntegrationTest {

    @Test
    public void testFullPipelineIntegration(@TempDir Path tempDir) throws Exception {
        // 1. Setup temporary CSV and PDF sources with duplicate emails and missing fields
        Path csvPath = tempDir.resolve("recruiter.csv");
        // CSV shares the email "john.doe@email.com" (duplicate) and has another uppercase duplicate "JOHN.DOE@EMAIL.COM"
        // Also has missing values for phone and current_company
        List<String> csvLines = List.of(
                "name,email,phone,current_company,title",
                "John D.,JOHN.DOE@EMAIL.COM,,,Staff Engineer"
        );
        Files.write(csvPath, csvLines);

        Path pdfPath = tempDir.resolve("resume.pdf");
        SampleGenerator.generatePdf(pdfPath.toFile()); // Creates Resume with "john.doe@email.com", location "United States", and phone "+1 415 555 2671"

        // 2. Parse and Normalize
        RecruiterCsvParser csvParser = new RecruiterCsvParser();
        CandidateProfile csvRaw = csvParser.parse(csvPath.toFile());
        CandidateProfile csvNorm = ProfileNormalizer.normalize(csvRaw);

        ResumePdfParser pdfParser = new ResumePdfParser();
        CandidateProfile pdfRaw = pdfParser.parse(pdfPath.toFile());
        CandidateProfile pdfNorm = ProfileNormalizer.normalize(pdfRaw);

        List<CandidateProfile> sourceList = List.of(pdfNorm, csvNorm);

        // Assert CSV duplicate email is normalized to lowercase
        assertTrue(csvNorm.getEmails().contains("john.doe@email.com"));

        // 3. Merge & Conflict Resolution (Resume PDF > Recruiter CSV)
        ConflictResolver resolver = new ConflictResolver(new SourcePriorityConflictResolutionStrategy(Constants.DEFAULT_SOURCE_PRIORITIES));
        MergeEngine mergeEngine = new MergeEngine(resolver);
        CandidateProfile merged = mergeEngine.merge(sourceList);

        // Verification of priority resolution: full_name from Resume PDF (John Doe) should override CSV (John D.)
        assertEquals("John Doe", merged.getFullName());
        assertEquals("Resume PDF", merged.getProvenance().get("fullName").getSource());

        // Verification of Email deduplication: only 1 instance of "john.doe@email.com" should exist
        assertEquals(1, merged.getEmails().size());
        assertEquals("john.doe@email.com", merged.getEmails().get(0));

        // Verification of Phone merge (CSV was blank, PDF had value)
        assertEquals(1, merged.getPhones().size());
        assertEquals("+14155552671", merged.getPhones().get(0));

        // 4. Confidence Calculation
        ConfidenceEngine confidenceEngine = new ConfidenceEngine();
        CandidateProfile canonical = confidenceEngine.calculateConfidence(merged, sourceList);

        // Verified confidence fields
        assertNotNull(canonical.getOverallConfidence());
        assertTrue(canonical.getOverallConfidence() > 0.85);

        // 5. Projection
        ProjectionEngine projectionEngine = new ProjectionEngine();
        Map<String, Object> config = new LinkedHashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(Map.of("path", "full_name"));
        fields.add(Map.of("path", "emails"));
        fields.add(Map.of("path", "location"));
        // missing field mapped to null
        fields.add(Map.of("path", "missing_links", "from", "non_existent"));

        config.put("fields", fields);
        config.put("include_confidence", true);
        config.put("on_missing", "null");

        Map<String, Object> projected = projectionEngine.project(canonical, config);

        assertEquals("John Doe", projected.get("full_name"));
        assertNull(projected.get("missing_links")); // missing values become null or omit according to config
        assertEquals("US", projected.get("location")); // Location United States normalized to US

        // 6. JSON Schema Validation
        try (InputStream schemaStream = getClass().getClassLoader().getResourceAsStream(Constants.DEFAULT_SCHEMA_PATH)) {
            assertNotNull(schemaStream, "Schema validation file must be accessible");
            SchemaValidator validator = new SchemaValidator(schemaStream);

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String jsonOutput = mapper.writeValueAsString(projected);

            // Note: projected format matches selected properties and shouldn't fail schema check
            boolean isValid = validator.validate(jsonOutput);
            // Since we didn't include candidate_id which is required in the default schema:
            // schema validation will return false, but we can verify it fails cleanly without throwing
            assertFalse(isValid);
        }
    }
}

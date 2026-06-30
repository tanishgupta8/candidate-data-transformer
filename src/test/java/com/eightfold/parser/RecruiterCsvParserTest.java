package com.eightfold.parser;

import com.eightfold.model.CandidateProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class RecruiterCsvParserTest {
    private RecruiterCsvParser parser;

    @BeforeEach
    public void setUp() {
        parser = new RecruiterCsvParser();
    }

    @Test
    public void testParseValidCsv(@TempDir Path tempDir) throws IOException {
        Path csvPath = tempDir.resolve("valid_recruiter.csv");
        List<String> content = List.of(
                "name,email,phone,current_company,title",
                "John Doe,john.doe@email.com,+1 (415) 555-2671,Google,Senior Software Engineer"
        );
        Files.write(csvPath, content);

        CandidateProfile profile = parser.parse(csvPath.toFile());

        assertNotNull(profile);
        assertEquals("John Doe", profile.getFullName());
        assertTrue(profile.getEmails().contains("john.doe@email.com"));
        assertTrue(profile.getPhones().contains("+1 (415) 555-2671"));
        assertEquals("Senior Software Engineer", profile.getHeadline());
        assertEquals(1, profile.getExperience().size());
        assertEquals("Google", profile.getExperience().get(0).getCompany());
        assertEquals("Senior Software Engineer", profile.getExperience().get(0).getTitle());
    }

    @Test
    public void testParseCsvWithMissingValues(@TempDir Path tempDir) throws IOException {
        Path csvPath = tempDir.resolve("missing_fields.csv");
        // phone and company are blank/missing
        List<String> content = List.of(
                "name,email,phone,current_company,title",
                "John Doe,john.doe@email.com,,,Software Architect"
        );
        Files.write(csvPath, content);

        CandidateProfile profile = parser.parse(csvPath.toFile());

        assertNotNull(profile);
        assertEquals("John Doe", profile.getFullName());
        assertTrue(profile.getEmails().contains("john.doe@email.com"));
        assertTrue(profile.getPhones().isEmpty());
        assertEquals("Software Architect", profile.getHeadline());
    }

    @Test
    public void testMalformedCsvThrowsParseException(@TempDir Path tempDir) {
        Path csvPath = tempDir.resolve("malformed.csv");
        // write some completely unformatted junk
        try {
            Files.writeString(csvPath, "\"name\",\"email\nJohn Doe,john.doe@email.com");
        } catch (IOException e) {
            fail("Failed to set up malformed file");
        }

        File file = csvPath.toFile();
        assertThrows(com.eightfold.exception.ParseException.class, () -> parser.parse(file));
    }
}

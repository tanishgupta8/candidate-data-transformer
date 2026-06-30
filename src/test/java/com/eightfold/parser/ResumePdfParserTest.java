package com.eightfold.parser;

import com.eightfold.model.CandidateProfile;
import com.eightfold.util.SampleGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public final class ResumePdfParserTest {
    private ResumePdfParser parser;

    @BeforeEach
    public void setUp() {
        parser = new ResumePdfParser();
    }

    @Test
    public void testParseValidPdfResume(@TempDir Path tempDir) throws IOException {
        Path pdfPath = tempDir.resolve("test_resume.pdf");
        File pdfFile = pdfPath.toFile();

        // Generate PDF using standard generator
        SampleGenerator.generatePdf(pdfFile);

        CandidateProfile profile = parser.parse(pdfFile);

        assertNotNull(profile);
        assertEquals("John Doe", profile.getFullName());
        assertTrue(profile.getEmails().contains("john.doe@email.com"));
        assertTrue(profile.getPhones().stream().anyMatch(p -> p.contains("415 555 2671")));
        assertTrue(profile.getLinks().stream().anyMatch(l -> l.contains("github.com/johndoe")));

        // Test skills parsed
        assertTrue(profile.getSkills().stream().anyMatch(s -> s.getName().equalsIgnoreCase("java")));
        assertTrue(profile.getSkills().stream().anyMatch(s -> s.getName().equalsIgnoreCase("nodejs")));

        // Test experiences parsed
        assertFalse(profile.getExperience().isEmpty());
        assertTrue(profile.getExperience().stream().anyMatch(e -> e.getCompany().equalsIgnoreCase("Google")));

        // Test education parsed
        assertFalse(profile.getEducation().isEmpty());
        assertTrue(profile.getEducation().stream().anyMatch(edu -> edu.getInstitution().equalsIgnoreCase("Stanford University")));
    }

    @Test
    public void testParseMalformedPdfThrowsParseException(@TempDir Path tempDir) throws IOException {
        Path corruptedPdfPath = tempDir.resolve("corrupted.pdf");
        // Write standard text junk to a .pdf extension file
        Files.writeString(corruptedPdfPath, "completely invalid pdf content");

        // The parser should throw ParseException on malformed/corrupted PDF content
        assertThrows(com.eightfold.exception.ParseException.class, () -> parser.parse(corruptedPdfPath.toFile()));
    }
}

package com.eightfold.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility program to generate sample source files (recruiter.csv and resume.pdf) in the workspace.
 */
public final class SampleGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SampleGenerator.class);

    public static void main(String[] args) {
        String baseDir = "resources/sample";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            generateCsv(new File(dir, "recruiter.csv"));
            generatePdf(new File(dir, "resume.pdf"));
            logger.info("Sample files generated successfully in: {}", dir.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Failed to generate sample files: {}", e.getMessage(), e);
        }
    }

    public static void generateCsv(File file) throws IOException {
        List<String> lines = List.of(
                "name,email,phone,current_company,title",
                "John Doe,john.doe@email.com,+1 (415) 555-2671,Google,Senior Software Engineer"
        );
        Files.write(file.toPath(), lines);
        logger.info("Generated recruiter.csv");
    }

    public static void generatePdf(File file) throws IOException {
        List<String> textLines = new ArrayList<>();
        textLines.add("John Doe");
        textLines.add("Email: john.doe@email.com | Phone: +1 415 555 2671 | Location: United States");
        textLines.add("Link: https://github.com/johndoe");
        textLines.add("");
        textLines.add("Summary:");
        textLines.add("Experienced Java Engineer specialized in building highly-scalable cloud solutions.");
        textLines.add("");
        textLines.add("Skills:");
        textLines.add("Java, NodeJS, React, AWS, Docker, Git, SQL");
        textLines.add("");
        textLines.add("Experience:");
        textLines.add("Google - Senior Software Engineer");
        textLines.add("2021-06 to Present");
        textLines.add("Worked on Google Cloud Platform and backend services using Java and Spring.");
        textLines.add("");
        textLines.add("Facebook - Software Engineer");
        textLines.add("2019-01 to 2021-05");
        textLines.add("Developed React-based user interfaces and Node.js microservices.");
        textLines.add("");
        textLines.add("Education:");
        textLines.add("Stanford University - M.S. in Computer Science");
        textLines.add("2017-09 to 2018-12");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 11);
                contentStream.setLeading(14.0f);
                contentStream.newLineAtOffset(50, 750);

                for (String line : textLines) {
                    contentStream.showText(line);
                    contentStream.newLine();
                }
                contentStream.endText();
            }
            document.save(file);
            logger.info("Generated resume.pdf");
        }
    }
}

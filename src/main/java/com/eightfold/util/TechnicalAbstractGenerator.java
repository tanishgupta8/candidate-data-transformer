package com.eightfold.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Programmatic PDF generator for the Technical Abstract page.
 */
public final class TechnicalAbstractGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TechnicalAbstractGenerator.class);

    private static final String OUTPUT_PDF_PATH = "Technical_Abstract.pdf";

    private static final String[] DIAGRAM_LINES = {
        "                    +----------------------+",
        "                    |   Recruiter CSV      |",
        "                    +----------+-----------+",
        "                               |",
        "                               |",
        "                    +----------v-----------+",
        "                    |    Resume PDF        |",
        "                    +----------+-----------+",
        "                               |",
        "                +--------------+--------------+",
        "                |     Source Parser Layer     |",
        "                +--------------+--------------+",
        "                               |",
        "                               v",
        "                +-----------------------------+",
        "                |     Data Normalization      |",
        "                | • Phone Numbers            |",
        "                | • Emails                   |",
        "                | • Skills                   |",
        "                | • Dates                    |",
        "                | • Countries                |",
        "                +--------------+--------------+",
        "                               |",
        "                               v",
        "                +-----------------------------+",
        "                | Merge & Conflict Resolver   |",
        "                +--------------+--------------+",
        "                               |",
        "                               v",
        "                +-----------------------------+",
        "                | Confidence Calculation      |",
        "                +--------------+--------------+",
        "                               |",
        "                               v",
        "                +-----------------------------+",
        "                | Canonical Candidate Model   |",
        "                +--------------+--------------+",
        "                               |",
        "                               v",
        "                +-----------------------------+",
        "                | Configurable Projection     |",
        "                +--------------+--------------+",
        "                               |",
        "                               v",
        "                +-----------------------------+",
        "                | JSON Schema Validation      |",
        "                +--------------+--------------+",
        "                               |",
        "                               v",
        "                +-----------------------------+",
        "                |     Output (profile.json)   |",
        "                +-----------------------------+"
    };

    public static void main(String[] args) {
        try {
            generate();
            System.out.println("Successfully generated Technical Abstract PDF at: " + OUTPUT_PDF_PATH);
        } catch (Exception e) {
            logger.error("Failed to generate technical abstract: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public static void generate() throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDFont fontRegular;
            PDFont fontBold;
            PDFont fontMono;

            // Attempt to load Windows fonts for Calibri and Consolas
            try {
                fontRegular = PDType0Font.load(document, new File("C:/Windows/Fonts/calibri.ttf"));
                fontBold = PDType0Font.load(document, new File("C:/Windows/Fonts/calibrib.ttf"));
                fontMono = PDType0Font.load(document, new File("C:/Windows/Fonts/consola.ttf"));
            } catch (Exception e) {
                logger.warn("Windows native fonts (Calibri/Consolas) not found. Falling back to built-in fonts.");
                fontRegular = PDType1Font.HELVETICA;
                fontBold = PDType1Font.HELVETICA_BOLD;
                fontMono = PDType1Font.COURIER;
            }

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                // Margins & Dimensions
                final float margin = 36f; // 0.5 inch margins
                final float pageWidth = page.getMediaBox().getWidth();
                final float pageHeight = page.getMediaBox().getHeight();
                final float printableWidth = pageWidth - (2 * margin);

                // 1. Draw Title Area (Centered, 18pt bold)
                content.beginText();
                content.setFont(fontBold, 18f);
                String title = "Multi-Source Candidate Data Transformer";
                float titleWidth = fontBold.getStringWidth(title) / 1000f * 18f;
                content.newLineAtOffset((pageWidth - titleWidth) / 2f, pageHeight - margin - 20f);
                content.showText(title);
                content.endText();

                // 2. Draw Subtitle (Centered, 12pt bold)
                content.beginText();
                content.setFont(fontBold, 12f);
                String subtitle = "Technical Abstract";
                float subtitleWidth = fontBold.getStringWidth(subtitle) / 1000f * 12f;
                content.newLineAtOffset((pageWidth - subtitleWidth) / 2f, pageHeight - margin - 40f);
                content.showText(subtitle);
                content.endText();

                // Separator line
                content.setLineWidth(1f);
                content.moveTo(margin, pageHeight - margin - 50f);
                content.lineTo(pageWidth - margin, pageHeight - margin - 50f);
                content.stroke();

                // Column setups
                final float leftColX = margin;
                final float leftColWidth = 305f;
                final float rightColX = margin + leftColWidth + 20f;
                final float rightColWidth = 205f;

                float leftY = pageHeight - margin - 70f;
                float rightY = pageHeight - margin - 70f;

                // Left Column - Content Flow
                leftY = drawSection(content, fontBold, fontRegular, "Problem Statement",
                        "Modern recruitment platforms receive candidate information from multiple heterogeneous sources " +
                        "such as recruiter spreadsheets, resumes, ATS exports, and online profiles. These sources often " +
                        "contain duplicate, incomplete, or conflicting information, making it difficult to create a reliable " +
                        "candidate profile. This project implements a configurable data transformation engine that consolidates " +
                        "information from multiple sources into a single canonical candidate profile while preserving provenance, " +
                        "calculating confidence scores, and validating the final output against a predefined schema.",
                        leftColX, leftY, leftColWidth);

                leftY = drawSection(content, fontBold, fontRegular, "Canonical Candidate Schema",
                        "The system transforms all input sources into a unified candidate profile consisting of: Candidate ID, " +
                        "Full Name, Email Addresses, Phone Numbers, Location, Professional Headline, Skills, Work Experience, " +
                        "Education, Provenance Metadata, and Overall Confidence Score. This canonical model enables consistent " +
                        "processing regardless of the source format.",
                        leftColX, leftY, leftColWidth);

                leftY = drawSection(content, fontBold, fontRegular, "Normalization Strategy",
                        "To improve data quality and consistency, the transformation engine performs: phone number normalization " +
                        "to E.164 international format; email standardization (trim, lowercase, duplicate removal); skill " +
                        "canonicalization (e.g., NodeJS, node js, NODE.JS → Node.js); date normalization to YYYY-MM format; country " +
                        "normalization using ISO-3166 Alpha-2 codes; and removal of duplicate values across all supported fields.",
                        leftColX, leftY, leftColWidth);

                leftY = drawSection(content, fontBold, fontRegular, "Conflict Resolution & Scoring",
                        "When conflicting values are encountered, the engine applies configurable source priorities. In the " +
                        "current implementation, Resume PDF > Recruiter CSV. Each extracted field stores its source of origin " +
                        "(provenance), resolution strategy, and confidence score. Fields from higher-priority sources receive higher " +
                        "confidence values. Agreement across multiple sources increases confidence, while conflicting values reduce it.",
                        leftColX, leftY, leftColWidth);

                leftY = drawSection(content, fontBold, fontRegular, "Projection & Validation",
                        "The internal canonical model is separated from the output representation using a configurable projection layer. " +
                        "This allows runtime customization of output fields without modifying the transformation logic. The generated " +
                        "JSON is validated against a predefined JSON Schema before being written to disk, ensuring structural " +
                        "consistency and robust error handling.",
                        leftColX, leftY, leftColWidth);

                leftY = drawSection(content, fontBold, fontRegular, "Conclusion",
                        "The proposed solution demonstrates a modular, scalable, and maintainable backend architecture capable of " +
                        "integrating heterogeneous candidate data sources into a unified and validated candidate profile. By leveraging " +
                        "object-oriented design principles, configurable processing, provenance tracking, confidence scoring, and schema " +
                        "validation, the system provides a robust foundation for future extensions.",
                        leftColX, leftY, leftColWidth);

                // Right Column - Diagram & Technologies Used
                rightY = drawSectionHeader(content, fontBold, "System Architecture", rightColX, rightY);
                rightY -= 10f; // Gap after heading

                // Draw ASCII diagram
                content.setFont(fontMono, 6.0f);
                final float lineSpacing = 7.5f;
                for (String line : DIAGRAM_LINES) {
                    // Trim leading spaces up to 16 characters
                    String trimmed = line.length() > 16 ? line.substring(16) : line.trim();
                    content.beginText();
                    content.newLineAtOffset(rightColX, rightY);
                    content.showText(trimmed);
                    content.endText();
                    rightY -= lineSpacing;
                }

                rightY -= 15f; // Gap after diagram

                // Draw Technologies Used in the right column to balance space
                drawSection(content, fontBold, fontRegular, "Technologies Used",
                        "Java 17 • Maven • Apache PDFBox • OpenCSV • Jackson • Google libphonenumber • JUnit 5 • SLF4J • Logback",
                        rightColX, rightY, rightColWidth);
            }

            document.save(OUTPUT_PDF_PATH);
        }
    }

    private static float drawSectionHeader(PDPageContentStream contentStream, PDFont fontBold, String title, float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(fontBold, 12f);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(title);
        contentStream.endText();
        return y - 14f;
    }

    private static float drawSection(PDPageContentStream contentStream, PDFont fontBold, PDFont fontRegular, String title, String bodyText, float x, float y, float width) throws IOException {
        y = drawSectionHeader(contentStream, fontBold, title, x, y);
        y -= 4f; // Margin between title and body

        List<String> wrappedLines = wrapText(bodyText, fontRegular, 9.5f, width);
        contentStream.setFont(fontRegular, 9.5f);

        for (int i = 0; i < wrappedLines.size(); i++) {
            String line = wrappedLines.get(i);
            boolean isLastLine = (i == wrappedLines.size() - 1);

            contentStream.beginText();
            contentStream.newLineAtOffset(x, y);

            if (isLastLine) {
                // Don't justify the last line of a paragraph
                contentStream.setWordSpacing(0f);
                contentStream.showText(line);
            } else {
                // Justify standard lines
                float textWidth = fontRegular.getStringWidth(line) / 1000f * 9.5f;
                int spaces = countSpaces(line);
                if (spaces > 0 && textWidth < width) {
                    float wordSpacing = (width - textWidth) / spaces;
                    contentStream.setWordSpacing(wordSpacing);
                } else {
                    contentStream.setWordSpacing(0f);
                }
                contentStream.showText(line);
            }

            contentStream.endText();
            y -= 12.5f; // Body leading
        }

        return y - 10f; // Gap after paragraph
    }

    private static List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> result = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float width = font.getStringWidth(testLine) / 1000f * fontSize;
            if (width > maxWidth) {
                result.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }
        if (currentLine.length() > 0) {
            result.add(currentLine.toString());
        }
        return result;
    }

    private static int countSpaces(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                count++;
            }
        }
        return count;
    }
}

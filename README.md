# Multi-Source Candidate Data Transformer

An enterprise-grade, configurable data integration pipeline designed to ingest candidate profiles from structured (CSV) and unstructured (PDF) sources, normalize them, resolve conflicting values, assign confidence scores, track data provenance, dynamically project fields, and perform JSON Schema validation.

---

## 1. Project Overview & Problem Statement

### Project Overview
In modern talent acquisition platforms, candidate details arrive via multiple channels—including recruiter uploads, resume PDFs, external profiles (LinkedIn, GitHub), and applicant tracking systems (ATS). These inputs are often inconsistent in formatting, incomplete in metadata, and vary in reliability.

The **Multi-Source Candidate Data Transformer** coordinates the ingestion, normalization, merging, and validation of candidate profiles into a standardized format suitable for down-stream machine learning search, scoring, and talent indexing.

### Problem Statement
Integrating candidate profiles from disparate sources poses several design challenges:
1. **Unstructured Data Ingestion**: Resumes are free-form PDFs that must be parsed using heuristics to segment details.
2. **Conflicting Values**: A recruiter's CSV might label a candidate "John D.", while their PDF resume says "John Doe". The engine must resolve these using priority rules.
3. **Data Quality and Provenance**: Recruiter databases need to trace *why* a particular value won out, what the confidence level is, and where the data originally came from.
4. **Flexible Output Formatting**: Different downstream databases require different subsets of profile fields. The system must dynamically restructure outputs at runtime.

---

## 2. Pipeline Architecture

Below is the execution flow of the data transformer:

```text
    [ Recruiter CSV ]         [ Resume PDF ]
           |                         |
           v (Load)                  v (Load)
      +----+----+               +----+----+
      |  Parser |               |  Parser |  (Factory Pattern - SourceParser)
      +----+----+               +----+----+
           |                         |
           v (Raw Candidate)         v (Raw Candidate)
      +----+-------------------------+----+
      |         ProfileNormalizer         |  (Strategy Pattern - NormalizerStrategy)
      +----------------+------------------+
                       |
                       v (Normalized Candidates)
               +-------+-------+
               |  Merge Engine |  (LeetCode-style Overlap Date Merging)
               +-------+-------+
                       |
                       v (Merged Profile)
               +-------+-------+
               |   Confidence  |  (Weighted Confidence Averaging)
               +-------+-------+
                       |
                       v (Canonical Candidate Profile)
               +-------+-------+
               |   Projector   |  (Configuration-Driven Projection)
               +-------+-------+
                       |
                       v (Projected Candidate Map)
               +-------+-------+
               |   Validator   |  (Draft-07 Schema Validation)
               +-------+-------+
                       |
                       v (Validated Payload)
                 [ Output JSON ]
```

---

## 3. Design Decisions (The "Why")

### Why Factory Pattern?
The system utilizes a `ParserFactory` to select the correct `SourceParser` implementation at runtime based on file extensions. This keeps the coordinator decoupled from concrete parsing classes and allows adding new source formats (e.g. JSON, DOCX) by simply implementing `SourceParser` and registering it in the factory, conforming to the Open-Closed Principle (OCP).

### Why Strategy Pattern?
- **Normalization Strateges**: The orchestrator decouples formatting logic by defining a `NormalizerStrategy` interface. Strategies for Phone, Skill, Date, and Country are registered as singletons, isolating modification risks.
- **Conflict Resolution Strategies**: The `ConflictResolver` delegates resolution tasks to a `ConflictResolutionStrategy`. By default, it runs priority-based resolution (`SourcePriorityConflictResolutionStrategy`), but can easily accept machine learning or recency-based resolution strategies.

### Why Canonical Model?
Ingesting multiple sources directly into a single target causes a combinatorial explosion of validation and formatting rules. Establishing a single `CandidateProfile` intermediate canonical model maps $N$ sources to $1$ model, ensuring that normalizers, merging algorithms, and confidence scoring work against a unified, predictable structure.

### Why Projection Layer?
Downstream consumer services frequently require different JSON payloads (e.g. search indexing needs name/skills; marketing alerts need emails; ATS needs experiences). A dedicated `ProjectionEngine` separates raw canonical candidate representations from custom, client-specific JSON maps, keeping the core domain clean.

### Why Source Parsers are Separated?
Separating parser classes (CSV and PDF) isolates dependency requirements (OpenCSV vs. Apache PDFBox) and makes testing/mocking individual parsing blocks highly maintainable, conforming to the Single Responsibility Principle (SRP).

### Why JSON Schema Validation?
Relying solely on Java class types does not guarantee downstream compliance with JSON restrictions (e.g., email patterns, array lengths, required fields). Schema validation enforces these constraints at the boundary before writing outputs.

### Why Configuration-Driven Projection?
Hardcoding output schemas forces re-compilation and re-deployment whenever a consumer client changes their JSON requirements. A config-driven projector dynamically maps, renames, indexes, and filters properties at runtime using JSON/YAML configuration maps.

---

## 4. Folder Structure

```text
candidate-transformer/
├── pom.xml
├── README.md
├── TECHNICAL_DESIGN.md
├── FINAL_REVIEW.md
├── resources/
│   ├── config/
│   │   ├── default.json              # Default projection configuration
│   │   └── custom.json               # Custom projection configuration
│   ├── schema/
│   │   └── candidate-schema.json     # JSON Schema validation file
│   └── sample/
│       ├── recruiter.csv             # Mock recruiter CSV
│       └── resume.pdf                # Mock candidate resume PDF
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/eightfold/
│   │   │       ├── Main.java         # CLI runner and entrypoint
│   │   │       ├── exception/        # Custom pipeline exceptions
│   │   │       │   ├── TransformerException.java
│   │   │       │   ├── ParseException.java
│   │   │       │   ├── ProjectionException.java
│   │   │       │   └── ValidationException.java
│   │   │       ├── model/            # Immutable domain models
│   │   │       │   ├── CandidateProfile.java
│   │   │       │   ├── Skill.java
│   │   │       │   ├── Experience.java
│   │   │       │   ├── Education.java
│   │   │       │   └── Provenance.java
│   │   │       ├── parser/           # Document parsers
│   │   │       │   ├── SourceParser.java
│   │   │       │   ├── ParserFactory.java
│   │   │       │   ├── RecruiterCsvParser.java
│   │   │       │   └── ResumePdfParser.java
│   │   │       ├── normalizer/       # Normalization strategies
│   │   │       │   ├── NormalizerStrategy.java
│   │   │       │   ├── PhoneNormalizer.java
│   │   │       │   ├── SkillNormalizer.java
│   │   │       │   ├── DateNormalizer.java
│   │   │       │   ├── CountryNormalizer.java
│   │   │       │   └── ProfileNormalizer.java
│   │   │       ├── merger/           # Consolidation & merging
│   │   │       │   ├── ConflictResolutionStrategy.java
│   │   │       │   ├── SourcePriorityConflictResolutionStrategy.java
│   │   │       │   ├── ConflictResolver.java
│   │   │       │   └── MergeEngine.java
│   │   │       ├── confidence/       # Scoring engine
│   │   │       │   └── ConfidenceEngine.java
│   │   │       ├── projection/       # Dynamic mapper
│   │   │       │   └── ProjectionEngine.java
│   │   │       ├── validation/       # Schema validation
│   │   │       │   └── SchemaValidator.java
│   │   │       └── util/             # Helper classes
│   │   │           ├── JsonUtil.java
│   │   │           ├── FileUtil.java
│   │   │           ├── Constants.java
│   │   │           └── SampleGenerator.java
│   │   └── resources/
│   │       ├── logback.xml
│   │       ├── config/
│   │       │   ├── default.json
│   │       │   └── custom.json
│   │       └── schema/
│   │           └── candidate-schema.json
│   └── test/
│       └── java/
│           └── com/eightfold/
│               ├── PipelineIntegrationTest.java
│               ├── normalizer/
│               │   ├── PhoneNormalizerTest.java
│               │   └── SkillNormalizerTest.java
│               ├── merger/
│               │   └── ConflictResolverTest.java
│               ├── projection/
│               │   └── ProjectionEngineTest.java
│               └── parser/
│                   ├── RecruiterCsvParserTest.java
│                   └── ResumePdfParserTest.java
```

---

## 5. Key Technologies & Dependencies

*   **Java 17** (Shaded/executable setup)
*   **Apache PDFBox 2.0.29** (Text extraction from unstructured PDFs)
*   **OpenCSV 5.7.1** (Case-insensitive column header mapping for CSV files)
*   **Google libphonenumber 8.13.15** (International E.164 phone formatting)
*   **Jackson Databind 2.15.2** (JSON serialization & YAML/JSON parsing)
*   **NetworkNT JSON Schema Validator 1.0.82** (Draft-07 validation)
*   **Logback + SLF4J** (Runtime application logging)
*   **JUnit 5 & Mockito** (Unit & integration test suites)

---

## 6. Installation & Build Instructions

Ensure you have **JDK 17** and **Maven 3.6+** installed.

### Build Executable Fat JAR
Compile source code, run tests, and package shaded dependencies:
```bash
mvn clean package
```

To run packaging while skipping test suites:
```bash
mvn package -DskipTests
```
The shaded executable JAR file will be output to `target/candidate-transformer.jar`.

---

## 7. Run Instructions & CLI Examples

### Step 1: Generate Sample Source Documents
Generate mock candidate recruiter CSV files and resumes into the local resources directory:
```bash
java -cp target/candidate-transformer.jar com.eightfold.util.SampleGenerator
```

### Step 2: Run CLI Pipeline (Standard Run)
Run the pipeline against sample files using the default configuration file:
```bash
java -jar target/candidate-transformer.jar \
  --csv resources/sample/recruiter.csv \
  --resume resources/sample/resume.pdf \
  --config resources/config/default.json \
  --output output/profile.json
```

### Step 3: Run CLI Pipeline (Custom Projection)
Run the pipeline using a custom output projection map:
```bash
java -jar target/candidate-transformer.jar \
  --csv resources/sample/recruiter.csv \
  --resume resources/sample/resume.pdf \
  --config resources/config/custom.json \
  --output output/custom_profile.json
```

---

## 8. Sample Inputs & Output

### Sample Input: Recruiter CSV (`recruiter.csv`)
```text
name,email,phone,current_company,title
John D.,JOHN.DOE@EMAIL.COM,,,Staff Engineer
```

### Sample Input: Resume PDF Text (`resume.pdf`)
```text
John Doe
Email: john.doe@email.com | Phone: +1 (415) 555-2671
Location: United States
Experience:
Google - Senior Software Engineer (2021-06 to Present)
Skills: Java, Node.js, GCP
Education:
Stanford University - Master of Science (2018-09 to 2020-06)
```

### Sample Output JSON (`profile.json`)
```json
{
  "candidate_id" : "7e5ab4b9-8c65-4f01-9457-4581297db3c9",
  "full_name" : "John Doe",
  "emails" : [ "john.doe@email.com" ],
  "phones" : [ "+14155552671" ],
  "location" : "US",
  "headline" : "Senior Software Engineer",
  "links" : [ ],
  "years_experience" : 5.1,
  "skills" : [ {
    "name" : "Java"
  }, {
    "name" : "Node.js"
  }, {
    "name" : "GCP"
  } ],
  "experience" : [ {
    "title" : "Senior Software Engineer",
    "company" : "Google",
    "startDate" : "2021-06",
    "endDate" : "Present",
    "description" : "Google - Senior Software Engineer"
  } ],
  "education" : [ {
    "degree" : "Master of Science",
    "institution" : "Stanford University",
    "fieldOfStudy" : "",
    "startDate" : "2018-09",
    "endDate" : "2020-06"
  } ],
  "_confidence" : {
    "fullName" : 0.95,
    "location" : 0.95,
    "headline" : 0.8,
    "yearsExperience" : 0.95,
    "emails" : 1.0,
    "phones" : 0.95,
    "links" : 0.95,
    "skills" : 0.95,
    "experience" : 0.95,
    "education" : 0.95
  },
  "_provenance" : {
    "fullName" : {
      "field" : "fullName",
      "source" : "Resume PDF",
      "method" : "source_priority"
    },
    "location" : {
      "field" : "location",
      "source" : "Resume PDF",
      "method" : "sole_source"
    },
    "headline" : {
      "field" : "headline",
      "source" : "Resume PDF",
      "method" : "source_priority"
    },
    "yearsExperience" : {
      "field" : "yearsExperience",
      "source" : "Calculated",
      "method" : "date_diff_sum"
    },
    "emails" : {
      "field" : "emails",
      "source" : "Resume PDF, Recruiter CSV",
      "method" : "merge"
    },
    "phones" : {
      "field" : "phones",
      "source" : "Resume PDF",
      "method" : "sole_source"
    },
    "links" : {
      "field" : "links",
      "source" : "Resume PDF",
      "method" : "sole_source"
    },
    "skills" : {
      "field" : "skills",
      "source" : "Resume PDF",
      "method" : "sole_source"
    },
    "experience" : {
      "field" : "experience",
      "source" : "Resume PDF, Recruiter CSV",
      "method" : "merge"
    },
    "education" : {
      "field" : "education",
      "source" : "Resume PDF",
      "method" : "sole_source"
    }
  }
}
```

---

## 9. Conflict Resolution, Confidence, & Provenance

### Conflict Resolution Strategy
Single-value fields are resolved dynamically using `SourcePriorityConflictResolutionStrategy` configured with source rankings. For example, if `Resume PDF > Recruiter CSV`, details in the PDF resume will override conflicts in the CSV recruiter records. Arrays are merged and deduplicated.

### Confidence Calculation Strategy
- **Baseline**: `Resume PDF` = `0.95`, `Recruiter CSV` = `0.90`.
- **Agreement**: Boost of `+0.05` (capped at `1.0`) if values are identical.
- **Conflict**: Penalty of `-0.10` if values differ.
- **Overall Weighted Average**: Double weight (`2.0`) is given to key identity fields (`fullName`, `emails`, `phones`), while other fields carry standard weight (`1.0`).

### Provenance Tracking
Each resolved property stores details in a `Provenance` entry:
- `source`: Chosen source document (e.g. `Resume PDF`, `Recruiter CSV`, or `Resume PDF, Recruiter CSV` for merged fields).
- `method`: Origin type (`sole_source`, `source_priority`, `merge`, or `date_diff_sum`).

---

## 10. Projection Layer & Validation

### Projection Config
Configured in `resources/config/default.json`:
```json
{
  "fields": [
    { "path": "candidate_id", "required": true },
    { "path": "full_name" },
    { "path": "emails" },
    { "path": "phones" },
    { "path": "location" },
    { "path": "headline" },
    { "path": "links" },
    { "path": "years_experience" },
    { "path": "skills" },
    { "path": "experience" },
    { "path": "education" }
  ],
  "include_confidence": true,
  "include_provenance": true,
  "on_missing": "null"
}
```

### JSON Schema
Located in `resources/schema/candidate-schema.json`, schema validation ensures the projected JSON complies with specific patterns (e.g. telephone formats, email regexes).

---

## 11. Error Handling & Assumptions

### Error Handling Policy
- Custom Exceptions are mapped to domain-specific entities (`ParseException`, `ProjectionException`, `ValidationException`).
- Individual parse/normalization errors are wrapped and logged as warnings in `Main.java`. They do not stop processing, allowing the engine to successfully emit partial outputs from readable candidate files.

### Core Assumptions
1. Resumes contain information for a single candidate.
2. Resume PDF has higher precedence than Recruiter CSV records.
3. Phone numbers that fail libphonenumber validation are ignored.
4. Overlapping date intervals are merged (preventing double-counting experience).

---

## 12. Future Scope

*   **Parsers**: Integrate LinkedIn HTML and GitHub API metadata parsers.
*   **Services**: Expose the core engine as a REST API endpoint using Spring Boot.
*   **Infrastructure**: Package the utility within Docker containers for cloud deployments.
*   **Performance**: Use parallel ThreadPool ExecutorServices to process batches of candidate files.

---

## 13. Testing

Run tests with Maven:
```bash
mvn test
```

### Coverage Scope:
- Malformed inputs (corrupted CSV and PDF).
- Identity duplication (emails, phones).
- Custom configuration projection errors.
- Algorithmic interval merging checks.

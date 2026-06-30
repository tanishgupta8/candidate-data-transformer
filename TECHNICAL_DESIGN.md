# Technical Design: Multi-Source Candidate Data Transformer

## 1. Problem Statement

Talent acquisition systems process candidate data from varying sources: structured tables (e.g. Recruiter CSVs) and unstructured files (e.g. Resume PDFs). Each source has different levels of completeness, formatting quality, and trustworthiness. 

The **Multi-Source Candidate Data Transformer** solves this by providing a modular pipeline that:
1. Ingests and parses structured (CSV) and unstructured (PDF) candidate data.
2. Normalizes phone numbers, dates, country names, skills, and email addresses.
3. Consolidates multiple profiles using prioritized conflict resolution.
4. Calculates confidence metrics per field and overall.
5. Projects custom JSON payloads dynamically based on runtime mappings.
6. Validates outputs against a schema without terminating execution on errors.

---

## 2. Pipeline Design

Below is the execution sequence for processing candidate profile inputs:

```text
  [ Recruiter CSV ]         [ Resume PDF ]
         |                         |
         v                         v
   +-----------+             +-----------+
   |   Parse   |             |   Parse   | (ParserFactory, PDFBox, OpenCSV)
   +-----+-----+             +-----+-----+
         |                         |
         v                         v
   +-----------+             +-----------+
   | Normalize |             | Normalize | (ProfileNormalizer, libphonenumber, Locale)
   +-----+-----+             +-----+-----+
         |                         |
         +------------+------------+
                      |
                      v
               +-------------+
               |    Merge    | (MergeEngine, ConflictResolver)
               +------+------+
                      |
                      v
               +-------------+
               | Confidence  | (ConfidenceEngine: Agree/Conflict adjustments)
               +------+------+
                      |
                      v
               +-------------+
               | Projector   | (ProjectionEngine: Select, Rename, Index, Nest)
               +------+------+
                      |
                      v
               +-------------+
               | Validate    | (SchemaValidator, NetworkNT JSON Schema Validator)
               +------+------+
                      |
                      v
                [ Output JSON ]
```

---

## 3. Canonical Candidate Schema

The pipeline merges profiles into an internal canonical candidate model represented by the following structure:

```json
{
  "candidate_id": "UUID string",
  "full_name": "String",
  "emails": ["Array of distinct normalized strings"],
  "phones": ["Array of distinct E.164 strings"],
  "location": "String (ISO-3166 alpha-2 country code)",
  "links": ["Array of web strings"],
  "headline": "String",
  "years_experience": "Double",
  "skills": [
    { "name": "String" }
  ],
  "experience": [
    {
      "title": "String",
      "company": "String",
      "startDate": "YYYY-MM",
      "endDate": "YYYY-MM or Present",
      "description": "String"
    }
  ],
  "education": [
    {
      "degree": "String",
      "institution": "String",
      "fieldOfStudy": "String",
      "startDate": "YYYY-MM",
      "endDate": "YYYY-MM"
    }
  ],
  "provenance": {
    "field_name": {
      "field": "String",
      "source": "String",
      "method": "String"
    }
  },
  "overall_confidence": "Double"
}
```

---

## 4. Normalization Strategy

| Field | Input Variation | Target Format | Technique |
| :--- | :--- | :--- | :--- |
| **Phone** | `+1 (415) 555-2671` | `+14155552671` (E.164) | Google `libphonenumber` parser with default region fallback. |
| **Date** | `June 2021`, `2021/06/15` | `2021-06` (YYYY-MM) | Pre-configured `DateTimeFormatter` list; "Present" keywords map to "Present". |
| **Country** | `United States`, `USA`, `US` | `US` (ISO-3166 alpha-2) | JDK Locales database query with a custom alias mapping. |
| **Skill** | `nodejs`, `NODE.JS`, `node js` | `Node.js` (Canonical) | Multi-variation key mapping; title-case fallback for unrecognized skills. |
| **Email** | ` John.Doe@Email.COM ` | `john.doe@email.com` | Standard trim, lowercase conversion, and deduplication filter. |

---

## 5. Conflict Resolution & Provenance

When the same field is populated across multiple candidate profiles:
*   **Precedence Ordering**: Resolved using a prioritized list of sources. By default, `Resume PDF > Recruiter CSV`.
*   **Conflict Handling**: For single-value fields (e.g. `full_name`), the value from the highest-priority source is retained. 
*   **Collection Merging**: Collection fields (e.g., `skills`, `emails`) are combined across all sources and de-duplicated.
*   **Provenance Logging**: Every field resolution records metadata detailing the source (e.g., `Resume PDF`) and resolution mechanism (e.g. `source_priority` for resolution, `merge` for collections, `sole_source` if present in only one profile).

---

## 6. Confidence Scoring Strategy

Confidence is computed dynamically to qualify the merged canonical profile:
1.  **Baseline Score**: Determined by the source priority:
    *   `Resume PDF` = `0.95`
    *   `Recruiter CSV` = `0.90`
    *   Unrecognized source = `0.50`
2.  **Agreement Bonus**: If both sources report identical values, a `+0.05` bonus is added (capped at `1.0`).
3.  **Conflict Penalty**: If both sources report values but they differ, a `-0.10` penalty is subtracted.
4.  **Overall Weighted Average**: Evaluates the profile's confidence level, applying a weight of `2.0` to identity parameters (`fullName`, `emails`, `phones`) and `1.0` to others.

---

## 7. Dynamic Projection Layer

The projection layer reshapes the immutable canonical model to suit runtime configurations.

```json
{
  "fields": [
    { "path": "id", "from": "candidate_id", "required": true },
    { "path": "name", "from": "full_name" },
    { "path": "primary_email", "from": "emails[0]" },
    { "path": "current_employer", "from": "experience[0].company" }
  ],
  "include_confidence": true,
  "include_provenance": false,
  "on_missing": "omit"
}
```

*   **Custom Mapping**: Resolves dot-separated sub-properties (`experience[0].company`) and array indexes (`emails[0]`) cleanly.
*   **Metadata Integration**: Optionally appends field-level `_confidence` and `_provenance` logs.
*   **Missing Field Handling**: Action policies based on configuration:
    *   `null`: Write key as null.
    *   `omit`: Remove key entirely from output.
    *   `error`: Halt execution with a detailed path exception if the field was marked `required`.

---

## 8. Schema Validation & Error Tolerance

*   **Open Schema Check**: Validates output using the draft-07 NetworkNT schema validator.
*   **Graceful Recovery**: Catches parsing, format, and schema violations internally. The engine logs errors as warnings and writes the final JSON rather than crashing.
*   **Missing Field Defaults**: Unresolved fields evaluate to null or omit according to projection policies.

---

## 9. Edge Cases & Algorithmic Handling

1.  **Missing Headers**: Parser defaults to position-based scanning if headers are unrecognized.
2.  **Overlapping Dates (Non-Double-Counting)**: Instead of naively summing durations (which double-counts parallel roles), the engine sorts candidate experience intervals and applies an interval-merging algorithm (similar to resolving overlapping intervals). The consolidated durations are then used to calculate `years_experience`.
3.  **Ambiguous Country Names**: Custom locale string cleaners match sub-strings (e.g. "San Francisco, USA" matches "USA" -> "US").
4.  **Single Candidate Processing**: The design assumes CSV contains information for the same candidate as the PDF, consolidating their values.

---

## 10. Architectural Strategy Patterns & SOLID Compliance

The codebase is built strictly around interfaces to achieve SOLID compliance:
*   **Normalizer Strategy (`NormalizerStrategy<T>`)**: Declares the strategy signature for normalization. Individual field strategies (Phone, Skill, Date, Country) implement this and are coordinated by `ProfileNormalizer`, removing static coupling.
*   **Conflict Resolution Strategy (`ConflictResolutionStrategy`)**: Defines the resolution method interface, permitting alternative resolution engines (e.g. ML-based, source priority) to be injected into `ConflictResolver` without modifications.
*   **Source Parser (`SourceParser`)**: Abstract template parser allowing the pipeline to support new formats (JSON, DOCX) by simply introducing a new implementation class.

---

## 11. Future Scope & Extensibility

*   **REST API Layer**: Designed with lightweight services so a Spring Boot controller can easily wrap the pipeline.
*   **External Taxonomy Integration**: Integrating APIs like EMSI or LinkedIn taxonomy to support dynamic skill mapping.
*   **Conflict Override Settings**: Adding field-specific priority overrides (e.g., choosing CSV over PDF specifically for phone numbers).

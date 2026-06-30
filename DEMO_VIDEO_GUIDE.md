# Multi-Source Candidate Data Transformer: Demo Video Guide
**Target Duration**: 2 Minutes (120 Seconds)

This guide provides a step-by-step storyboard and a matching voiceover script to help you record your demonstration video.

---

## 1. Visual Sequence & Concept Labels (Storyboard)

| Time | Screen Visual | Concept Label (On-Screen Action) |
| :--- | :--- | :--- |
| **0:00 - 0:15** | IDE open showing `README.md` or file layout. | **[CONCEPT: Project Overview]** Show project directories. |
| **0:15 - 0:40** | Terminal. Run `mvn clean package` and execute default command. | **[CONCEPT: End-to-End Default Pipeline]** Run CLI and show execution summary card. |
| **0:40 - 1:10** | Open `resources/config/custom.json` and then show `output/custom_profile.json` side-by-side. | **[CONCEPT: Runtime Configuration Projection]** Highlight dynamic field remapping/filtering. |
| **1:10 - 1:35** | Open `NormalizerStrategy.java` and `ConflictResolver.java`. | **[CONCEPT: Strategy Pattern Design]** Show decoupled interfaces. |
| **1:35 - 1:55** | Open `MergeEngine.java` at `calculateYearsExperience`. | **[CONCEPT: Overlapping Date Interval Merging]** Highlight interval sort-and-merge loop. |
| **1:55 - 2:00** | GitHub repository open in browser. | **[CONCEPT: GitHub Delivery & Conclusion]** Show project pushed to Git. |

---

## 2. Voiceover Script (120-Second Word-for-Word)

### Introduction (0:00 - 0:15)
> *"Hello! Today I’m demoing the Multi-Source Candidate Data Transformer. This is a configurable Java 17 pipeline designed to ingest raw candidate profiles from structured CSV databases and unstructured PDF resumes, normalizes the data, resolves conflicting fields, and formats standard JSON outputs."*

### Default Execution (0:15 - 0:40)
> *(Action: Run CLI command for default run in the terminal)*
> *"Let's run the pipeline using the shaded executable JAR. The program compiles, loads the CSV and PDF resumes, standardizes emails, parses skills, and writes the output. On completion, the CLI prints this clean Execution Summary block showing details on fields extracted, resolved conflicts, and our weighted overall confidence score."*

### Custom Configuration Run (0:40 - 1:10)
> *(Action: Open custom.json and custom_profile.json)*
> *"A key requirement is dynamic projection. If we execute the CLI passing a custom config file, we can rename keys and filter fields at runtime. In the output on the right, you can see `candidate_id` was mapped to `id`, `full_name` to `name`, and deep properties like `experience[0].company` were extracted as `current_employer`. In addition, provenance metadata was completely excluded, keeping the payload lightweight."*

### Design Decision (1:10 - 1:35)
> *(Action: Highlight NormalizerStrategy and ConflictResolver code)*
> *"One design choice I'm proud of is using the **Strategy Pattern** for normalizers and conflict resolvers. Rather than coupling our engine with static utilities, all formatting routines (like E.164 phone normalization or country lookups) implement a common strategy interface. This keeps the codebase open for new extensions but completely closed to core modifications, honoring the open-closed principle."*

### Edge Case Handling (1:35 - 1:55)
> *(Action: Highlight calculateYearsExperience in MergeEngine.java)*
> *"For edge case handling, we solved **overlapping work tenures**. A naive approach to calculate years of experience simply sums job durations, which double-counts overlapping timelines if a candidate worked concurrent roles. We solved this by sorting the experience intervals and applying a classic interval-merging algorithm to calculate actual, consolidated years of experience."*

### Conclusion (1:55 - 2:00)
> *"The project is fully tested with 100% compliance and is published on GitHub. Thank you for watching!"*

---

## 3. Preparation Tips
1. **Screen Resolution**: Set your display to $1920 \times 1080$ and increase your IDE/terminal font size slightly so the reviewer can read the code.
2. **Clear output directories**: Delete files in `output/` before starting, so the video captures them being generated live.
3. **Pace**: Read the script naturally and pause for a second when switching files.

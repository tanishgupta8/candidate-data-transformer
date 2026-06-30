package com.eightfold.projection;

import com.eightfold.exception.ProjectionException;
import com.eightfold.model.CandidateProfile;
import com.eightfold.model.Education;
import com.eightfold.model.Experience;
import com.eightfold.model.Provenance;
import com.eightfold.model.Skill;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Transforms the immutable canonical profile into a customized output structure based on runtime configuration.
 */
public final class ProjectionEngine {
    private static final Logger logger = LoggerFactory.getLogger(ProjectionEngine.class);
    private final ObjectMapper objectMapper;

    public ProjectionEngine() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Projects the candidate profile into a map based on a configuration map.
     *
     * @param profile the CandidateProfile to project.
     * @param configMap the configuration specifying fields and options.
     * @return the projected data map.
     * @throws ProjectionException if validation checks fail or required fields are missing with error policy enabled.
     */
    public Map<String, Object> project(CandidateProfile profile, Map<String, Object> configMap) {
        if (profile == null) {
            return Collections.emptyMap();
        }

        logger.info("Projecting output according to runtime configuration...");

        // Parse config map
        ProjectionConfig config = objectMapper.convertValue(configMap, ProjectionConfig.class);
        Map<String, Object> output = new LinkedHashMap<>();

        boolean includeConfidence = Boolean.TRUE.equals(config.includeConfidence);
        boolean includeProvenance = Boolean.TRUE.equals(config.includeProvenance);
        String onMissing = config.onMissing != null ? config.onMissing.trim().toLowerCase() : "null";

        Map<String, Double> confidences = new LinkedHashMap<>();
        Map<String, Object> provenances = new LinkedHashMap<>();

        if (config.fields == null || config.fields.isEmpty()) {
            // Default: project all canonical fields
            projectAllFields(profile, output, confidences, provenances);
        } else {
            for (FieldConfig field : config.fields) {
                String sourcePath = field.from != null ? field.from : field.path;
                Object value = evaluatePath(profile, sourcePath);

                if (value == null || (value instanceof Collection && ((Collection<?>) value).isEmpty())) {
                    if (Boolean.TRUE.equals(field.required)) {
                        if ("error".equals(onMissing)) {
                            throw new ProjectionException("Required field '" + field.path + "' is missing (source path: " + sourcePath + ").");
                        }
                    }

                    if ("omit".equals(onMissing)) {
                        continue;
                    } else { // "null" is default
                        output.put(field.path, null);
                    }
                } else {
                    output.put(field.path, value);
                }

                // Gather field confidence & provenance if enabled
                String baseField = getBaseFieldName(sourcePath);
                if (includeConfidence) {
                    Double score = profile.getFieldConfidences().get(baseField);
                    if (score != null) {
                        confidences.put(field.path, score);
                    } else if (field.path.equals("overall_confidence") || field.path.equals("overallConfidence")) {
                        confidences.put(field.path, profile.getOverallConfidence());
                    }
                }

                if (includeProvenance) {
                    Provenance prov = profile.getProvenance().get(baseField);
                    if (prov != null) {
                        provenances.put(field.path, prov);
                    }
                }
            }
        }

        if (includeConfidence && !confidences.isEmpty()) {
            output.put("_confidence", confidences);
        }
        if (includeProvenance && !provenances.isEmpty()) {
            output.put("_provenance", provenances);
        }

        return output;
    }

    private void projectAllFields(CandidateProfile profile, Map<String, Object> output, Map<String, Double> confidences, Map<String, Object> provenances) {
        output.put("candidate_id", profile.getCandidateId());
        output.put("full_name", profile.getFullName());
        output.put("emails", profile.getEmails());
        output.put("phones", profile.getPhones());
        output.put("location", profile.getLocation());
        output.put("links", profile.getLinks());
        output.put("headline", profile.getHeadline());
        output.put("years_experience", profile.getYearsExperience());
        output.put("skills", profile.getSkills());
        output.put("experience", profile.getExperience());
        output.put("education", profile.getEducation());
        output.put("overall_confidence", profile.getOverallConfidence());

        confidences.putAll(profile.getFieldConfidences());
        provenances.putAll(profile.getProvenance());
    }

    private Object evaluatePath(CandidateProfile profile, String path) {
        if (path == null || path.isBlank()) return null;

        String[] segments = path.split("\\.");
        Object current = profile;
        for (String segment : segments) {
            if (current == null) return null;
            current = evaluateSegment(current, segment);
        }

        // Auto-extract skill name if returning a Skill object directly at the end of path
        if (current instanceof Skill) {
            return ((Skill) current).getName();
        }
        return current;
    }

    private Object evaluateSegment(Object obj, String segment) {
        if (segment.contains("[")) {
            try {
                String base = segment.substring(0, segment.indexOf("[")).trim();
                String indexStr = segment.substring(segment.indexOf("[") + 1, segment.indexOf("]")).trim();
                int index = Integer.parseInt(indexStr);

                Object listObj = getProperty(obj, base);
                if (listObj instanceof List) {
                    List<?> list = (List<?>) listObj;
                    if (index >= 0 && index < list.size()) {
                        return list.get(index);
                    }
                }
            } catch (Exception e) {
                logger.debug("Failed evaluating index segment {}: {}", segment, e.getMessage());
            }
            return null;
        }

        return getProperty(obj, segment);
    }

    private Object getProperty(Object obj, String property) {
        if (obj == null || property == null) return null;
        String normalizedProp = normalizeFieldKey(property);

        if (obj instanceof CandidateProfile) {
            return getFieldByName((CandidateProfile) obj, normalizedProp);
        } else if (obj instanceof Experience) {
            Experience exp = (Experience) obj;
            switch (normalizedProp) {
                case "title": return exp.getTitle();
                case "company": return exp.getCompany();
                case "startDate": return exp.getStartDate();
                case "endDate": return exp.getEndDate();
                case "description": return exp.getDescription();
            }
        } else if (obj instanceof Education) {
            Education edu = (Education) obj;
            switch (normalizedProp) {
                case "degree": return edu.getDegree();
                case "institution": return edu.getInstitution();
                case "fieldOfStudy": return edu.getFieldOfStudy();
                case "startDate": return edu.getStartDate();
                case "endDate": return edu.getEndDate();
            }
        } else if (obj instanceof Skill) {
            return ((Skill) obj).getName();
        }
        return null;
    }

    private String getBaseFieldName(String path) {
        if (path == null) return "";
        // Extract first segment of the path as the base field
        String firstSegment = path.split("\\.")[0];
        String base = firstSegment.contains("[") ? firstSegment.substring(0, firstSegment.indexOf("[")) : firstSegment;
        return normalizeFieldKey(base.trim());
    }

    private String normalizeFieldKey(String key) {
        if (key == null) return "";
        // Convert snake_case to camelCase
        if (key.contains("_")) {
            StringBuilder sb = new StringBuilder();
            String[] parts = key.split("_");
            sb.append(parts[0]);
            for (int i = 1; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    sb.append(Character.toUpperCase(parts[i].charAt(0)));
                    if (parts[i].length() > 1) {
                        sb.append(parts[i].substring(1));
                    }
                }
            }
            return sb.toString();
        }
        return key;
    }

    private Object getFieldByName(CandidateProfile profile, String path) {
        switch (path) {
            case "candidateId": return profile.getCandidateId();
            case "fullName": return profile.getFullName();
            case "emails": return profile.getEmails();
            case "phones": return profile.getPhones();
            case "location": return profile.getLocation();
            case "links": return profile.getLinks();
            case "headline": return profile.getHeadline();
            case "yearsExperience": return profile.getYearsExperience();
            case "skills": return profile.getSkills();
            case "experience": return profile.getExperience();
            case "education": return profile.getEducation();
            case "overallConfidence": return profile.getOverallConfidence();
            default: return null;
        }
    }

    // Static nested configuration DTOs
    public static class ProjectionConfig {
        public List<FieldConfig> fields;
        public Boolean includeConfidence;
        public Boolean includeProvenance;
        public String onMissing; // "null", "omit", "error"

        public void setFields(List<FieldConfig> fields) { this.fields = fields; }
        public void setInclude_confidence(Boolean val) { this.includeConfidence = val; }
        public void setInclude_provenance(Boolean val) { this.includeProvenance = val; }
        public void setOn_missing(String val) { this.onMissing = val; }
    }

    public static class FieldConfig {
        public String path;
        public String from;
        public String type;
        public Boolean required;
    }
}

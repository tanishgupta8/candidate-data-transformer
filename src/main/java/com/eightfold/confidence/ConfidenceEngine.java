package com.eightfold.confidence;

import com.eightfold.model.CandidateProfile;
import com.eightfold.model.Provenance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Calculates field-level and overall confidence scores for a merged candidate profile.
 */
public final class ConfidenceEngine {
    private static final Logger logger = LoggerFactory.getLogger(ConfidenceEngine.class);

    private static final double BASE_RESUME = 0.95;
    private static final double BASE_CSV = 0.90;
    private static final double BASE_UNKNOWN = 0.50;

    private static final double AGREEMENT_BONUS = 0.05;
    private static final double CONFLICT_PENALTY = 0.10;

    /**
     * Calculates confidence scores and attaches them to the merged profile.
     *
     * @param merged the merged CandidateProfile.
     * @param sources the individual source profiles before merge.
     * @return a new CandidateProfile with populated fieldConfidences and overallConfidence.
     */
    public CandidateProfile calculateConfidence(CandidateProfile merged, List<CandidateProfile> sources) {
        logger.info("Calculating confidence scores...");
        CandidateProfile.Builder builder = CandidateProfile.builder(merged);

        Map<String, Double> fieldConfidences = new HashMap<>();

        // List of fields to score
        String[] fields = {"fullName", "location", "headline", "yearsExperience", "emails", "phones", "links", "skills", "experience", "education"};

        // Weight map for calculating overall weighted average
        Map<String, Double> weights = new HashMap<>();
        weights.put("fullName", 2.0);
        weights.put("emails", 2.0);
        weights.put("phones", 2.0);
        weights.put("location", 1.0);
        weights.put("headline", 1.0);
        weights.put("yearsExperience", 1.0);
        weights.put("skills", 1.0);
        weights.put("experience", 1.0);
        weights.put("education", 1.0);
        weights.put("links", 1.0);

        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;

        for (String field : fields) {
            Double score = calculateFieldConfidence(field, merged, sources);
            if (score != null) {
                fieldConfidences.put(field, score);
                builder.putFieldConfidence(field, score);

                double weight = weights.getOrDefault(field, 1.0);
                totalWeightedScore += score * weight;
                totalWeight += weight;
            }
        }

        double overallConfidence = totalWeight > 0 ? (totalWeightedScore / totalWeight) : 0.0;
        // Round to 3 decimal places
        overallConfidence = Math.round(overallConfidence * 1000.0) / 1000.0;
        builder.overallConfidence(overallConfidence);

        logger.info("Overall confidence score: {}", overallConfidence);
        return builder.build();
    }

    private Double calculateFieldConfidence(String field, CandidateProfile merged, List<CandidateProfile> sources) {
        // Find which sources had this field populated
        List<SourceValue> values = new ArrayList<>();
        for (CandidateProfile srcProfile : sources) {
            Object val = getFieldValue(srcProfile, field);
            if (val != null && !isEmptyValue(val)) {
                String sourceName = getSourceForField(srcProfile, field);
                values.add(new SourceValue(val, sourceName));
            }
        }

        if (values.isEmpty()) {
            return null; // Field not populated in any source
        }

        // Determine baseline score based on the source chosen in the merged profile
        Provenance mergedProv = merged.getProvenance().get(field);
        String chosenSource = mergedProv != null ? mergedProv.getSource() : "Unknown";
        double baseScore = getBaselineScore(chosenSource);

        if (values.size() == 1) {
            // Sole source - return baseline score
            return baseScore;
        }

        // Multiple sources - check if they agree
        boolean agree = true;
        Object firstVal = values.get(0).value;
        for (int i = 1; i < values.size(); i++) {
            if (!areValuesEqual(firstVal, values.get(i).value)) {
                agree = false;
                break;
            }
        }

        double finalScore;
        if (agree) {
            finalScore = Math.min(1.0, baseScore + AGREEMENT_BONUS);
            logger.debug("Field '{}' sources agree. Confidence boosted to {}", field, finalScore);
        } else {
            finalScore = Math.max(0.0, baseScore - CONFLICT_PENALTY);
            logger.debug("Field '{}' sources conflict. Confidence penalized to {}", field, finalScore);
        }

        return Math.round(finalScore * 1000.0) / 1000.0;
    }

    private double getBaselineScore(String sourceName) {
        if (sourceName == null) return BASE_UNKNOWN;
        if (sourceName.contains("Resume PDF")) return BASE_RESUME;
        if (sourceName.contains("Recruiter CSV")) return BASE_CSV;
        return BASE_UNKNOWN;
    }

    private Object getFieldValue(CandidateProfile profile, String field) {
        switch (field) {
            case "fullName": return profile.getFullName();
            case "location": return profile.getLocation();
            case "headline": return profile.getHeadline();
            case "yearsExperience": return profile.getYearsExperience();
            case "emails": return profile.getEmails();
            case "phones": return profile.getPhones();
            case "links": return profile.getLinks();
            case "skills": return profile.getSkills();
            case "experience": return profile.getExperience();
            case "education": return profile.getEducation();
            default: return null;
        }
    }

    private String getSourceForField(CandidateProfile profile, String field) {
        Provenance prov = profile.getProvenance().get(field);
        return prov != null ? prov.getSource() : "Unknown";
    }

    private boolean isEmptyValue(Object val) {
        if (val instanceof String) return ((String) val).isBlank();
        if (val instanceof Collection) return ((Collection<?>) val).isEmpty();
        return false;
    }

    private boolean areValuesEqual(Object v1, Object v2) {
        if (v1 == null || v2 == null) return v1 == v2;
        if (v1 instanceof String && v2 instanceof String) {
            return ((String) v1).trim().equalsIgnoreCase(((String) v2).trim());
        }
        if (v1 instanceof Collection && v2 instanceof Collection) {
            Collection<?> c1 = (Collection<?>) v1;
            Collection<?> c2 = (Collection<?>) v2;
            return c1.size() == c2.size() && c1.containsAll(c2);
        }
        return v1.equals(v2);
    }

    private static class SourceValue {
        final Object value;
        final String source;

        SourceValue(Object value, String source) {
            this.value = value;
            this.source = source;
        }
    }
}

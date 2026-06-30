package com.eightfold.normalizer;

import com.eightfold.model.CandidateProfile;
import com.eightfold.model.Education;
import com.eightfold.model.Experience;
import com.eightfold.model.Skill;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Orchestrates the normalization phase using registered NormalizerStrategies.
 */
public final class ProfileNormalizer {

    private static final NormalizerStrategy<String> phoneNormalizer = PhoneNormalizer.INSTANCE;
    private static final NormalizerStrategy<String> skillNormalizer = SkillNormalizer.INSTANCE;
    private static final NormalizerStrategy<String> dateNormalizer = DateNormalizer.INSTANCE;
    private static final NormalizerStrategy<String> countryNormalizer = CountryNormalizer.INSTANCE;

    private ProfileNormalizer() {}

    /**
     * Normalizes all components of a candidate profile using strategies.
     *
     * @param raw the raw CandidateProfile.
     * @return a new normalized CandidateProfile.
     */
    public static CandidateProfile normalize(CandidateProfile raw) {
        if (raw == null) {
            return null;
        }

        CandidateProfile.Builder builder = CandidateProfile.builder();

        builder.candidateId(raw.getCandidateId());

        if (raw.getFullName() != null) {
            builder.fullName(raw.getFullName().trim());
        }

        // Email normalization: trim, lowercase, distinct, non-empty
        if (raw.getEmails() != null) {
            List<String> normalizedEmails = raw.getEmails().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            builder.emails(normalizedEmails);
        }

        // Phone normalization: use E.164 phone strategy
        if (raw.getPhones() != null) {
            List<String> normalizedPhones = raw.getPhones().stream()
                    .filter(Objects::nonNull)
                    .map(phoneNormalizer::normalize)
                    .flatMap(Optional::stream)
                    .distinct()
                    .collect(Collectors.toList());
            builder.phones(normalizedPhones);
        }

        // Location country normalization: ISO-3166 alpha-2 strategy
        if (raw.getLocation() != null) {
            String normLoc = countryNormalizer.normalize(raw.getLocation())
                    .orElse(raw.getLocation().trim());
            builder.location(normLoc);
        }

        if (raw.getHeadline() != null) {
            builder.headline(raw.getHeadline().trim());
        }

        if (raw.getLinks() != null) {
            List<String> normalizedLinks = raw.getLinks().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            builder.links(normalizedLinks);
        }

        builder.yearsExperience(raw.getYearsExperience());

        // Skills normalization: canonicalize strategy
        if (raw.getSkills() != null) {
            List<Skill> normalizedSkills = raw.getSkills().stream()
                    .filter(Objects::nonNull)
                    .map(Skill::getName)
                    .map(skillNormalizer::normalize)
                    .flatMap(Optional::stream)
                    .distinct()
                    .map(Skill::new)
                    .collect(Collectors.toList());
            builder.skills(normalizedSkills);
        }

        // Experience normalization: date normalization strategy
        if (raw.getExperience() != null) {
            List<Experience> normalizedExp = raw.getExperience().stream()
                    .filter(Objects::nonNull)
                    .map(exp -> new Experience(
                            exp.getTitle(),
                            exp.getCompany(),
                            dateNormalizer.normalize(exp.getStartDate()).orElse(exp.getStartDate()),
                            dateNormalizer.normalize(exp.getEndDate()).orElse(exp.getEndDate()),
                            exp.getDescription()
                    ))
                    .collect(Collectors.toList());
            builder.experience(normalizedExp);
        }

        // Education normalization: date normalization strategy
        if (raw.getEducation() != null) {
            List<Education> normalizedEdu = raw.getEducation().stream()
                    .filter(Objects::nonNull)
                    .map(edu -> new Education(
                            edu.getDegree(),
                            edu.getInstitution(),
                            edu.getFieldOfStudy(),
                            dateNormalizer.normalize(edu.getStartDate()).orElse(edu.getStartDate()),
                            dateNormalizer.normalize(edu.getEndDate()).orElse(edu.getEndDate())
                    ))
                    .collect(Collectors.toList());
            builder.education(normalizedEdu);
        }

        // Copy provenance and confidence mapping
        builder.provenance(raw.getProvenance());
        builder.fieldConfidences(raw.getFieldConfidences());
        builder.overallConfidence(raw.getOverallConfidence());

        return builder.build();
    }
}

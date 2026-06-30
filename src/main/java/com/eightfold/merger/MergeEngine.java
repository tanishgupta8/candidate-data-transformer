package com.eightfold.merger;

import com.eightfold.model.CandidateProfile;
import com.eightfold.model.Education;
import com.eightfold.model.Experience;
import com.eightfold.model.Provenance;
import com.eightfold.model.Skill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Merges multiple CandidateProfile records into one canonical profile using conflict resolution rules.
 */
public final class MergeEngine {
    private static final Logger logger = LoggerFactory.getLogger(MergeEngine.class);
    private final ConflictResolver conflictResolver;
    private int resolvedConflictsCount = 0;

    public MergeEngine(ConflictResolver conflictResolver) {
        this.conflictResolver = conflictResolver != null ? conflictResolver : new ConflictResolver(null);
    }

    public int getResolvedConflictsCount() {
        return resolvedConflictsCount;
    }

    /**
     * Merges a list of CandidateProfile records into a single consolidated CandidateProfile.
     *
     * @param profiles list of profiles to merge.
     * @return the merged CandidateProfile.
     */
    public CandidateProfile merge(List<CandidateProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return CandidateProfile.builder().build();
        }

        logger.info("Merging {} candidate profiles", profiles.size());
        this.resolvedConflictsCount = 0;

        // We will merge sequentially. Start with the first profile.
        CandidateProfile merged = profiles.get(0);

        for (int i = 1; i < profiles.size(); i++) {
            merged = mergeTwo(merged, profiles.get(i));
        }

        // Post-merge calculation of years of experience if not explicitly set
        Double computedYears = calculateYearsExperience(merged.getExperience());
        if (merged.getYearsExperience() == null || merged.getYearsExperience() == 0.0) {
            merged = CandidateProfile.builder(merged)
                    .yearsExperience(computedYears)
                    .putProvenance("yearsExperience", new Provenance("yearsExperience", "Calculated", "date_diff_sum"))
                    .build();
        }

        return merged;
    }

    private CandidateProfile mergeTwo(CandidateProfile p1, CandidateProfile p2) {
        if (p1.getFullName() != null && p2.getFullName() != null && !p1.getFullName().equals(p2.getFullName())) {
            resolvedConflictsCount++;
        }
        if (p1.getLocation() != null && p2.getLocation() != null && !p1.getLocation().equals(p2.getLocation())) {
            resolvedConflictsCount++;
        }
        if (p1.getHeadline() != null && p2.getHeadline() != null && !p1.getHeadline().equals(p2.getHeadline())) {
            resolvedConflictsCount++;
        }
        if (p1.getYearsExperience() != null && p2.getYearsExperience() != null && !p1.getYearsExperience().equals(p2.getYearsExperience())) {
            resolvedConflictsCount++;
        }

        CandidateProfile.Builder builder = CandidateProfile.builder();

        // Preserve candidate ID from the first profile
        builder.candidateId(p1.getCandidateId());

        // Resolve single-value fields and keep track of provenance
        String src1 = getSourceForField(p1, "fullName", "Profile 1");
        String src2 = getSourceForField(p2, "fullName", "Profile 2");
        ConflictResolver.ResolvedValue<String> nameRes = conflictResolver.resolve(p1.getFullName(), src1, p2.getFullName(), src2, "fullName");
        builder.fullName(nameRes.getValue());
        if (nameRes.getProvenance() != null) {
            builder.putProvenance("fullName", nameRes.getProvenance());
        }

        src1 = getSourceForField(p1, "location", "Profile 1");
        src2 = getSourceForField(p2, "location", "Profile 2");
        ConflictResolver.ResolvedValue<String> locRes = conflictResolver.resolve(p1.getLocation(), src1, p2.getLocation(), src2, "location");
        builder.location(locRes.getValue());
        if (locRes.getProvenance() != null) {
            builder.putProvenance("location", locRes.getProvenance());
        }

        src1 = getSourceForField(p1, "headline", "Profile 1");
        src2 = getSourceForField(p2, "headline", "Profile 2");
        ConflictResolver.ResolvedValue<String> headRes = conflictResolver.resolve(p1.getHeadline(), src1, p2.getHeadline(), src2, "headline");
        builder.headline(headRes.getValue());
        if (headRes.getProvenance() != null) {
            builder.putProvenance("headline", headRes.getProvenance());
        }

        src1 = getSourceForField(p1, "yearsExperience", "Profile 1");
        src2 = getSourceForField(p2, "yearsExperience", "Profile 2");
        ConflictResolver.ResolvedValue<Double> expYearsRes = conflictResolver.resolve(p1.getYearsExperience(), src1, p2.getYearsExperience(), src2, "yearsExperience");
        builder.yearsExperience(expYearsRes.getValue());
        if (expYearsRes.getProvenance() != null) {
            builder.putProvenance("yearsExperience", expYearsRes.getProvenance());
        }

        // Merge collection fields
        // Emails
        List<String> emails = new ArrayList<>(p1.getEmails());
        emails.addAll(p2.getEmails());
        List<String> distinctEmails = emails.stream().distinct().collect(Collectors.toList());
        builder.emails(distinctEmails);
        builder.putProvenance("emails", createCollectionProvenance("emails", p1, p2));

        // Phones
        List<String> phones = new ArrayList<>(p1.getPhones());
        phones.addAll(p2.getPhones());
        List<String> distinctPhones = phones.stream().distinct().collect(Collectors.toList());
        builder.phones(distinctPhones);
        builder.putProvenance("phones", createCollectionProvenance("phones", p1, p2));

        // Links
        List<String> links = new ArrayList<>(p1.getLinks());
        links.addAll(p2.getLinks());
        List<String> distinctLinks = links.stream().distinct().collect(Collectors.toList());
        builder.links(distinctLinks);
        builder.putProvenance("links", createCollectionProvenance("links", p1, p2));

        // Skills
        List<Skill> skills = new ArrayList<>(p1.getSkills());
        skills.addAll(p2.getSkills());
        List<Skill> distinctSkills = skills.stream().distinct().collect(Collectors.toList());
        builder.skills(distinctSkills);
        builder.putProvenance("skills", createCollectionProvenance("skills", p1, p2));

        // Experience
        List<Experience> experienceList = new ArrayList<>(p1.getExperience());
        experienceList.addAll(p2.getExperience());
        List<Experience> distinctExperience = experienceList.stream().distinct().collect(Collectors.toList());
        builder.experience(distinctExperience);
        builder.putProvenance("experience", createCollectionProvenance("experience", p1, p2));

        // Education
        List<Education> educationList = new ArrayList<>(p1.getEducation());
        educationList.addAll(p2.getEducation());
        List<Education> distinctEducation = educationList.stream().distinct().collect(Collectors.toList());
        builder.education(distinctEducation);
        builder.putProvenance("education", createCollectionProvenance("education", p1, p2));

        // Aggregate field confidences (ConfidenceEngine will calculate scores later, copy for now)
        Map<String, Double> mergedConfidences = new HashMap<>(p1.getFieldConfidences());
        p2.getFieldConfidences().forEach((k, v) -> mergedConfidences.merge(k, v, (oldVal, newVal) -> (oldVal + newVal) / 2.0));
        builder.fieldConfidences(mergedConfidences);

        return builder.build();
    }

    private String getSourceForField(CandidateProfile profile, String field, String defaultValue) {
        Provenance prov = profile.getProvenance().get(field);
        return prov != null ? prov.getSource() : defaultValue;
    }

    private Provenance createCollectionProvenance(String field, CandidateProfile p1, CandidateProfile p2) {
        Provenance prov1 = p1.getProvenance().get(field);
        Provenance prov2 = p2.getProvenance().get(field);

        String s1 = prov1 != null ? prov1.getSource() : null;
        String s2 = prov2 != null ? prov2.getSource() : null;

        if (s1 != null && s2 != null) {
            if (s1.equals(s2)) {
                return new Provenance(field, s1, "merge");
            }
            return new Provenance(field, s1 + ", " + s2, "merge");
        } else if (s1 != null) {
            return new Provenance(field, s1, "sole_source");
        } else if (s2 != null) {
            return new Provenance(field, s2, "sole_source");
        }
        return new Provenance(field, "Unknown", "merge");
    }

    private Double calculateYearsExperience(List<Experience> experiences) {
        if (experiences == null || experiences.isEmpty()) {
            return 0.0;
        }

        List<DateInterval> intervals = new ArrayList<>();
        for (Experience exp : experiences) {
            try {
                String startStr = exp.getStartDate();
                String endStr = exp.getEndDate();

                if (startStr == null || startStr.isBlank()) {
                    continue;
                }

                YearMonth start = YearMonth.parse(startStr.trim());
                YearMonth end;

                if (endStr == null || endStr.isBlank() || endStr.equalsIgnoreCase("present")) {
                    end = YearMonth.now();
                } else {
                    end = YearMonth.parse(endStr.trim());
                }

                if (start.isBefore(end) || start.equals(end)) {
                    intervals.add(new DateInterval(start, end));
                }
            } catch (Exception ignored) {
                // ignore parsing failures for malformed dates
            }
        }

        if (intervals.isEmpty()) {
            return 0.0;
        }

        // Sort intervals by start date
        Collections.sort(intervals);

        // Merge overlapping intervals
        List<DateInterval> mergedIntervals = new ArrayList<>();
        DateInterval current = intervals.get(0);

        for (int i = 1; i < intervals.size(); i++) {
            DateInterval next = intervals.get(i);
            // If next start date is before or equal to current end date, they overlap
            if (!current.end.isBefore(next.start)) {
                // Merge current and next by extending the end date to the maximum of both
                YearMonth maxEnd = current.end.isAfter(next.end) ? current.end : next.end;
                current = new DateInterval(current.start, maxEnd);
            } else {
                mergedIntervals.add(current);
                current = next;
            }
        }
        mergedIntervals.add(current);

        // Calculate total inclusive months across disjoint intervals
        double totalMonths = 0.0;
        for (DateInterval interval : mergedIntervals) {
            long months = ChronoUnit.MONTHS.between(interval.start, interval.end);
            totalMonths += (months + 1); // Add 1 for inclusive month mapping
        }

        return Math.round((totalMonths / 12.0) * 10.0) / 10.0;
    }

    private static class DateInterval implements Comparable<DateInterval> {
        final YearMonth start;
        final YearMonth end;

        DateInterval(YearMonth start, YearMonth end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int compareTo(DateInterval o) {
            return this.start.compareTo(o.start);
        }
    }
}

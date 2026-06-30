package com.eightfold.model;

import java.util.*;

/**
 * Immutable canonical profile representing the consolidated data of a candidate.
 */
public final class CandidateProfile {
    private final String candidateId;
    private final String fullName;
    private final List<String> emails;
    private final List<String> phones;
    private final String location;
    private final List<String> links;
    private final String headline;
    private final Double yearsExperience;
    private final List<Skill> skills;
    private final List<Experience> experience;
    private final List<Education> education;
    private final Map<String, Provenance> provenance;
    private final Map<String, Double> fieldConfidences;
    private final Double overallConfidence;

    private CandidateProfile(Builder builder) {
        this.candidateId = builder.candidateId != null ? builder.candidateId : UUID.randomUUID().toString();
        this.fullName = builder.fullName;
        this.emails = builder.emails != null ? Collections.unmodifiableList(new ArrayList<>(builder.emails)) : Collections.emptyList();
        this.phones = builder.phones != null ? Collections.unmodifiableList(new ArrayList<>(builder.phones)) : Collections.emptyList();
        this.location = builder.location;
        this.links = builder.links != null ? Collections.unmodifiableList(new ArrayList<>(builder.links)) : Collections.emptyList();
        this.headline = builder.headline;
        this.yearsExperience = builder.yearsExperience;
        this.skills = builder.skills != null ? Collections.unmodifiableList(new ArrayList<>(builder.skills)) : Collections.emptyList();
        this.experience = builder.experience != null ? Collections.unmodifiableList(new ArrayList<>(builder.experience)) : Collections.emptyList();
        this.education = builder.education != null ? Collections.unmodifiableList(new ArrayList<>(builder.education)) : Collections.emptyList();
        this.provenance = builder.provenance != null ? Collections.unmodifiableMap(new HashMap<>(builder.provenance)) : Collections.emptyMap();
        this.fieldConfidences = builder.fieldConfidences != null ? Collections.unmodifiableMap(new HashMap<>(builder.fieldConfidences)) : Collections.emptyMap();
        this.overallConfidence = builder.overallConfidence;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getEmails() {
        return emails;
    }

    public List<String> getPhones() {
        return phones;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getLinks() {
        return links;
    }

    public String getHeadline() {
        return headline;
    }

    public Double getYearsExperience() {
        return yearsExperience;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public List<Experience> getExperience() {
        return experience;
    }

    public List<Education> getEducation() {
        return education;
    }

    public Map<String, Provenance> getProvenance() {
        return provenance;
    }

    public Map<String, Double> getFieldConfidences() {
        return fieldConfidences;
    }

    public Double getOverallConfidence() {
        return overallConfidence;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(CandidateProfile prototype) {
        return new Builder(prototype);
    }

    @Override
    public String toString() {
        return "CandidateProfile{" +
                "candidateId='" + candidateId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", emails=" + emails +
                ", phones=" + phones +
                ", location='" + location + '\'' +
                ", yearsExperience=" + yearsExperience +
                ", overallConfidence=" + overallConfidence +
                '}';
    }

    /**
     * Builder pattern implementation for CandidateProfile.
     */
    public static class Builder {
        private String candidateId;
        private String fullName;
        private List<String> emails = new ArrayList<>();
        private List<String> phones = new ArrayList<>();
        private String location;
        private List<String> links = new ArrayList<>();
        private String headline;
        private Double yearsExperience;
        private List<Skill> skills = new ArrayList<>();
        private List<Experience> experience = new ArrayList<>();
        private List<Education> education = new ArrayList<>();
        private Map<String, Provenance> provenance = new HashMap<>();
        private Map<String, Double> fieldConfidences = new HashMap<>();
        private Double overallConfidence;

        public Builder() {}

        public Builder(CandidateProfile prototype) {
            if (prototype != null) {
                this.candidateId = prototype.candidateId;
                this.fullName = prototype.fullName;
                this.emails = new ArrayList<>(prototype.emails);
                this.phones = new ArrayList<>(prototype.phones);
                this.location = prototype.location;
                this.links = new ArrayList<>(prototype.links);
                this.headline = prototype.headline;
                this.yearsExperience = prototype.yearsExperience;
                this.skills = new ArrayList<>(prototype.skills);
                this.experience = new ArrayList<>(prototype.experience);
                this.education = new ArrayList<>(prototype.education);
                this.provenance = new HashMap<>(prototype.provenance);
                this.fieldConfidences = new HashMap<>(prototype.fieldConfidences);
                this.overallConfidence = prototype.overallConfidence;
            }
        }

        public Builder candidateId(String candidateId) {
            this.candidateId = candidateId;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder emails(List<String> emails) {
            this.emails = emails != null ? new ArrayList<>(emails) : new ArrayList<>();
            return this;
        }

        public Builder addEmail(String email) {
            if (email != null && !email.isBlank()) {
                this.emails.add(email);
            }
            return this;
        }

        public Builder phones(List<String> phones) {
            this.phones = phones != null ? new ArrayList<>(phones) : new ArrayList<>();
            return this;
        }

        public Builder addPhone(String phone) {
            if (phone != null && !phone.isBlank()) {
                this.phones.add(phone);
            }
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder links(List<String> links) {
            this.links = links != null ? new ArrayList<>(links) : new ArrayList<>();
            return this;
        }

        public Builder addLink(String link) {
            if (link != null && !link.isBlank()) {
                this.links.add(link);
            }
            return this;
        }

        public Builder headline(String headline) {
            this.headline = headline;
            return this;
        }

        public Builder yearsExperience(Double yearsExperience) {
            this.yearsExperience = yearsExperience;
            return this;
        }

        public Builder skills(List<Skill> skills) {
            this.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
            return this;
        }

        public Builder addSkill(Skill skill) {
            if (skill != null) {
                this.skills.add(skill);
            }
            return this;
        }

        public Builder experience(List<Experience> experience) {
            this.experience = experience != null ? new ArrayList<>(experience) : new ArrayList<>();
            return this;
        }

        public Builder addExperience(Experience exp) {
            if (exp != null) {
                this.experience.add(exp);
            }
            return this;
        }

        public Builder education(List<Education> education) {
            this.education = education != null ? new ArrayList<>(education) : new ArrayList<>();
            return this;
        }

        public Builder addEducation(Education edu) {
            if (edu != null) {
                this.education.add(edu);
            }
            return this;
        }

        public Builder provenance(Map<String, Provenance> provenance) {
            this.provenance = provenance != null ? new HashMap<>(provenance) : new HashMap<>();
            return this;
        }

        public Builder putProvenance(String field, Provenance prov) {
            if (field != null && prov != null) {
                this.provenance.put(field, prov);
            }
            return this;
        }

        public Builder fieldConfidences(Map<String, Double> fieldConfidences) {
            this.fieldConfidences = fieldConfidences != null ? new HashMap<>(fieldConfidences) : new HashMap<>();
            return this;
        }

        public Builder putFieldConfidence(String field, Double score) {
            if (field != null && score != null) {
                this.fieldConfidences.put(field, score);
            }
            return this;
        }

        public Builder overallConfidence(Double overallConfidence) {
            this.overallConfidence = overallConfidence;
            return this;
        }

        public CandidateProfile build() {
            return new CandidateProfile(this);
        }
    }
}

package com.eightfold.model;

import java.util.Objects;

/**
 * Immutable representation of a Candidate's education.
 */
public final class Education {
    private final String degree;
    private final String institution;
    private final String fieldOfStudy;
    private final String startDate; // YYYY-MM
    private final String endDate;   // YYYY-MM

    public Education(String degree, String institution, String fieldOfStudy, String startDate, String endDate) {
        this.degree = degree != null ? degree.trim() : "";
        this.institution = institution != null ? institution.trim() : "";
        this.fieldOfStudy = fieldOfStudy != null ? fieldOfStudy.trim() : "";
        this.startDate = startDate != null ? startDate.trim() : "";
        this.endDate = endDate != null ? endDate.trim() : "";
    }

    public String getDegree() {
        return degree;
    }

    public String getInstitution() {
        return institution;
    }

    public String getFieldOfStudy() {
        return fieldOfStudy;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Education education = (Education) o;
        return Objects.equals(degree, education.degree) &&
               Objects.equals(institution, education.institution) &&
               Objects.equals(fieldOfStudy, education.fieldOfStudy) &&
               Objects.equals(startDate, education.startDate) &&
               Objects.equals(endDate, education.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(degree, institution, fieldOfStudy, startDate, endDate);
    }

    @Override
    public String toString() {
        return "Education{" +
                "degree='" + degree + '\'' +
                ", institution='" + institution + '\'' +
                ", fieldOfStudy='" + fieldOfStudy + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                '}';
    }
}

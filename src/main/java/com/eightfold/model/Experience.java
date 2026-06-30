package com.eightfold.model;

import java.util.Objects;

/**
 * Immutable representation of a Candidate's professional experience.
 */
public final class Experience {
    private final String title;
    private final String company;
    private final String startDate; // YYYY-MM
    private final String endDate;   // YYYY-MM or "Present" / null
    private final String description;

    public Experience(String title, String company, String startDate, String endDate, String description) {
        this.title = title != null ? title.trim() : "";
        this.company = company != null ? company.trim() : "";
        this.startDate = startDate != null ? startDate.trim() : "";
        this.endDate = endDate != null ? endDate.trim() : "";
        this.description = description != null ? description.trim() : "";
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Experience that = (Experience) o;
        return Objects.equals(title, that.title) &&
               Objects.equals(company, that.company) &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate) &&
               Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, company, startDate, endDate, description);
    }

    @Override
    public String toString() {
        return "Experience{" +
                "title='" + title + '\'' +
                ", company='" + company + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

package com.eightfold.model;

import java.util.Objects;

/**
 * Represents the confidence level calculated for a specific candidate field.
 */
public final class ConfidenceScore {
    private final String field;
    private final double score;

    public ConfidenceScore(String field, double score) {
        this.field = field != null ? field.trim() : "";
        this.score = score;
    }

    public String getField() {
        return field;
    }

    public double getScore() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfidenceScore that = (ConfidenceScore) o;
        return Double.compare(that.score, score) == 0 &&
               Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, score);
    }

    @Override
    public String toString() {
        return "ConfidenceScore{" +
                "field='" + field + '\'' +
                ", score=" + score +
                '}';
    }
}

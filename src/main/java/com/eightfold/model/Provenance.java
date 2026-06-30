package com.eightfold.model;

import java.util.Objects;

/**
 * Tracks the provenance of a specific candidate field, including its source and resolution method.
 */
public final class Provenance {
    private final String field;
    private final String source;
    private final String method;

    public Provenance(String field, String source, String method) {
        this.field = field != null ? field.trim() : "";
        this.source = source != null ? source.trim() : "";
        this.method = method != null ? method.trim() : "";
    }

    public String getField() {
        return field;
    }

    public String getSource() {
        return source;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Provenance that = (Provenance) o;
        return Objects.equals(field, that.field) &&
               Objects.equals(source, that.source) &&
               Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, source, method);
    }

    @Override
    public String toString() {
        return "Provenance{" +
                "field='" + field + '\'' +
                ", source='" + source + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}

package com.eightfold.model;

import java.util.Objects;

/**
 * Immutable representation of a Candidate's Skill.
 */
public final class Skill {
    private final String name;

    public Skill(String name) {
        this.name = name != null ? name.trim() : "";
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Skill skill = (Skill) o;
        return Objects.equals(name, skill.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Skill{name='" + name + "'}";
    }
}

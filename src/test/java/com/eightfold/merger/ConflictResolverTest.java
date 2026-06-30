package com.eightfold.merger;

import com.eightfold.model.Provenance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class ConflictResolverTest {

    @Test
    public void testPriorityResolutionDefault() {
        ConflictResolver resolver = new ConflictResolver(new SourcePriorityConflictResolutionStrategy(List.of("Resume PDF", "Recruiter CSV")));

        // Both present: Resume PDF should win
        ConflictResolver.ResolvedValue<String> res = resolver.resolve(
                "John Doe", "Resume PDF",
                "John D.", "Recruiter CSV",
                "fullName"
        );

        assertEquals("John Doe", res.getValue());
        assertEquals("fullName", res.getProvenance().getField());
        assertEquals("Resume PDF", res.getProvenance().getSource());
        assertEquals("source_priority", res.getProvenance().getMethod());
    }

    @Test
    public void testPriorityResolutionReversed() {
        // Reverse standard priorities: Recruiter CSV wins
        ConflictResolver resolver = new ConflictResolver(new SourcePriorityConflictResolutionStrategy(List.of("Recruiter CSV", "Resume PDF")));

        ConflictResolver.ResolvedValue<String> res = resolver.resolve(
                "John Doe", "Resume PDF",
                "John D.", "Recruiter CSV",
                "fullName"
        );

        assertEquals("John D.", res.getValue());
        assertEquals("Recruiter CSV", res.getProvenance().getSource());
    }

    @Test
    public void testSoleSourceResolution() {
        ConflictResolver resolver = new ConflictResolver(new SourcePriorityConflictResolutionStrategy(null));

        // Only Resume PDF has value
        ConflictResolver.ResolvedValue<String> res1 = resolver.resolve(
                "John Doe", "Resume PDF",
                null, "Recruiter CSV",
                "fullName"
        );

        assertEquals("John Doe", res1.getValue());
        assertEquals("sole_source", res1.getProvenance().getMethod());

        // Only Recruiter CSV has value
        ConflictResolver.ResolvedValue<String> res2 = resolver.resolve(
                null, "Resume PDF",
                "John D.", "Recruiter CSV",
                "fullName"
        );

        assertEquals("John D.", res2.getValue());
        assertEquals("sole_source", res2.getProvenance().getMethod());
    }
}

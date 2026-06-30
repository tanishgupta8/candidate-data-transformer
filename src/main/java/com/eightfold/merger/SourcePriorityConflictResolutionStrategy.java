package com.eightfold.merger;

import com.eightfold.model.Provenance;
import java.util.List;

/**
 * Concrete conflict resolution strategy based on configurable source priorities.
 */
public final class SourcePriorityConflictResolutionStrategy implements ConflictResolutionStrategy {
    private final List<String> sourcePriorities;

    public SourcePriorityConflictResolutionStrategy(List<String> sourcePriorities) {
        this.sourcePriorities = sourcePriorities != null && !sourcePriorities.isEmpty()
                ? List.copyOf(sourcePriorities)
                : List.of("Resume PDF", "Recruiter CSV");
    }

    @Override
    public <T> ConflictResolver.ResolvedValue<T> resolve(T val1, String src1, T val2, String src2, String fieldName) {
        if (val1 == null && val2 == null) {
            return new ConflictResolver.ResolvedValue<>(null, null);
        }
        if (val1 != null && val2 == null) {
            return new ConflictResolver.ResolvedValue<>(val1, new Provenance(fieldName, src1, "sole_source"));
        }
        if (val1 == null && val2 != null) {
            return new ConflictResolver.ResolvedValue<>(val2, new Provenance(fieldName, src2, "sole_source"));
        }

        // Evaluate priority indexes
        int p1 = sourcePriorities.indexOf(src1);
        int p2 = sourcePriorities.indexOf(src2);

        int rank1 = p1 == -1 ? Integer.MAX_VALUE : p1;
        int rank2 = p2 == -1 ? Integer.MAX_VALUE : p2;

        if (rank1 <= rank2) {
            return new ConflictResolver.ResolvedValue<>(val1, new Provenance(fieldName, src1, "source_priority"));
        } else {
            return new ConflictResolver.ResolvedValue<>(val2, new Provenance(fieldName, src2, "source_priority"));
        }
    }
}

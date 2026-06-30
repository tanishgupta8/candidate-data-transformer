package com.eightfold.merger;

import com.eightfold.model.Provenance;

/**
 * Context class in the Strategy Pattern that delegates conflict resolution to a ConflictResolutionStrategy.
 */
public final class ConflictResolver {
    private final ConflictResolutionStrategy strategy;

    /**
     * Primary constructor accepting a specific resolution strategy.
     *
     * @param strategy the resolution strategy.
     */
    public ConflictResolver(ConflictResolutionStrategy strategy) {
        this.strategy = strategy != null ? strategy : new SourcePriorityConflictResolutionStrategy(null);
    }

    /**
     * Resolves the value conflict by delegating to the strategy.
     *
     * @param val1 first value.
     * @param src1 first value's source.
     * @param val2 second value.
     * @param src2 second value's source.
     * @param fieldName the name of the field.
     * @param <T> the type of field value.
     * @return the ResolvedValue.
     */
    public <T> ResolvedValue<T> resolve(T val1, String src1, T val2, String src2, String fieldName) {
        return strategy.resolve(val1, src1, val2, src2, fieldName);
    }

    /**
     * Holder class representing the result of a conflict resolution.
     */
    public static final class ResolvedValue<T> {
        private final T value;
        private final Provenance provenance;

        public ResolvedValue(T value, Provenance provenance) {
            this.value = value;
            this.provenance = provenance;
        }

        public T getValue() {
            return value;
        }

        public Provenance getProvenance() {
            return provenance;
        }
    }
}

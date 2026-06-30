package com.eightfold.merger;

/**
 * Strategy interface for resolving values when they conflict across multiple sources.
 */
public interface ConflictResolutionStrategy {
    /**
     * Resolves the conflict between two values and assigns provenance.
     *
     * @param val1 first value.
     * @param src1 first value's source.
     * @param val2 second value.
     * @param src2 second value's source.
     * @param fieldName the field name.
     * @param <T> the type of field.
     * @return ResolvedValue containing the selected value and provenance.
     */
    <T> ConflictResolver.ResolvedValue<T> resolve(T val1, String src1, T val2, String src2, String fieldName);
}

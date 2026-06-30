package com.eightfold.normalizer;

import java.util.Optional;

/**
 * Strategy interface for normalization components.
 *
 * @param <T> the type of data being normalized.
 */
public interface NormalizerStrategy<T> {
    /**
     * Normalizes the given input value.
     *
     * @param input the raw input.
     * @return an Optional containing the normalized value, or Optional.empty() if invalid.
     */
    Optional<T> normalize(T input);
}

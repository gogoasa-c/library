package com.github.gogoasac.domain.entity;

/**
 * Immutable value object representing a collection or shelf grouping.
 *
 * <p>Used to categorize books and produce reports and listings.
 */
public record Collection(
    Long id,
    String name) {

    @Override
    public String toString() {
        return String.format("Collection{id=%s, name='%s'}", id, name);
    }
}

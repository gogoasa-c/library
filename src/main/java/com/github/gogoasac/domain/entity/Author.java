package com.github.gogoasac.domain.entity;

/**
 * Immutable value object representing an author.
 *
 * <p>Simple record used across services, repositories and UI layers to represent
 * an author reference and display name.
 */
public record Author(
    Long id,
    String name) {

    @Override
    public String toString() {
        return String.format("Author{id=%s, name='%s'}", id, name);
    }
}

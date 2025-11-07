package com.github.gogoasac.domain.entity;

import java.time.LocalDate;

/**
 * Domain record representing an immutable Book within the library domain.
 *
 * <p>The record holds identifying information (id, title), references to related
 * entities (authorId, collectionId), a publication year and borrowing state
 * (borrowedAt timestamp and a boolean flag).
 *
 * <p>Responsibility:
 * - Provide an immutable data carrier for persistence, reporting and UI layers.
 * - Expose simple domain behaviour (e.g. {@link #borrow()}) that returns a new
 *   instance with updated borrowing state rather than mutating state.
 */
public record Book(
    Long id,
    String title,
    Long authorId,
    Long collectionId,
    Integer publicationYear,
    LocalDate borrowedAt,
    boolean isBorrowed) {

    public Book(Long id, String title, Long authorId, Long collectionId, Integer publicationYear) {
        this(id, title, authorId, collectionId, publicationYear, null, false);
    }

    public Book borrow() {
        if (isBorrowed) {
            throw new IllegalStateException("Book is already borrowed.");
        }
        return new Book(id, title, authorId, collectionId, publicationYear, LocalDate.now(), true);
    }

    @Override
    public String toString() {
        return String
            .format("Book{id=%s, title='%s', authorId=%s, collectionId=%s, year=%s, borrowedAt=%s, isBorrowed=%s}",
            id, title, authorId, collectionId, publicationYear, borrowedAt, isBorrowed);
    }
}

package com.github.gogoasac.domain.entity;

import java.time.LocalDate;

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

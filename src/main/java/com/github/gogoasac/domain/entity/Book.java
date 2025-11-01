package com.github.gogoasac.domain.entity;

public record Book(
    Long id,
    String title,
    Long authorId,
    Long collectionId,
    Integer publicationYear) {

    @Override
    public String toString() {
        return String.format("Book{id=%s, title='%s', authorId=%s, collectionId=%s, year=%s}",
            id, title, authorId, collectionId, publicationYear);
    }
}

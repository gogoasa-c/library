package com.github.gogoasac.domain.entity;

public record Book(
    Long id,
    String title,
    Long authorId,
    Long collectionId,
    Integer publicationYear) {
}

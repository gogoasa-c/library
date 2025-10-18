package com.github.gogoasac.application.dto;

public record AddBookCommand(
    String title,
    Long authorId,
    Long collectionId,
    int publicationYear
) {
}

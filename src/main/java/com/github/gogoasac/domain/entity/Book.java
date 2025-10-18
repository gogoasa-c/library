package com.github.gogoasac.domain.entity;

public record Book(
    Long id,
    String title,
    Author author,
    Collection collection,
    Integer publicationYear) {
}

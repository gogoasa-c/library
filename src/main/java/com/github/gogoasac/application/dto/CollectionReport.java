package com.github.gogoasac.application.dto;

import java.util.List;

public record CollectionReport(
    String collectionName,
    List<BookReport> books
) {
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Collection: ").append(collectionName).append("\n");
        books.forEach(b -> sb.append("  - ").append(b.toString()).append("\n"));
        return sb.toString();
    }
}

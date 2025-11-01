package com.github.gogoasac.application.dto;

public record BookReport(
    String title,
    String authorName
) {
    @Override
    public String toString() {
        return String.format("%s by %s", title, authorName);
    }
}

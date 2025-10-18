package com.github.gogoasac.application.dto;

import java.util.List;

public record CollectionReport(
    String collectionName,
    List<BookReport> books
) {}

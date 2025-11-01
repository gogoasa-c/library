package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.BookReport;
import com.github.gogoasac.application.dto.CollectionReport;
import com.github.gogoasac.application.input.ReportingInput;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.application.output.BookPersistence;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.domain.entity.Book;
import com.github.gogoasac.domain.entity.Collection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportingService implements ReportingInput {
    private final CollectionPersistence collectionPersistence;
    private final BookPersistence bookPersistence;
    private final AuthorPersistence authorPersistence;

    public ReportingService(CollectionPersistence collectionPersistence,
                            BookPersistence bookPersistence,
                            AuthorPersistence authorPersistence) {
        this.collectionPersistence = collectionPersistence;
        this.bookPersistence = bookPersistence;
        this.authorPersistence = authorPersistence;
    }

    @Override
    public List<CollectionReport> generateCollectionReports() {
        List<Collection> collections = collectionPersistence.findAll();
        Map<Long, List<Book>> booksByCollection = bookPersistence.findAll()
            .stream()
            .collect(Collectors.groupingBy(Book::collectionId));

        List<CollectionReport> reports = collections.stream()
            .map(collection -> new CollectionReport(
                collection.name(),
                booksByCollection.getOrDefault(collection.id(), List.of()).stream()
                    .map(this::mapToBookReport)
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());

        writeReportFile(reports);
        return reports;
    }

    private BookReport mapToBookReport(Book book) {
        Author author = authorPersistence.findById(book.authorId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Author with ID " + book.authorId() + " does not exist."));
        return new BookReport(book.title(), author.name());
    }

    private void writeReportFile(List<CollectionReport> reports) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String date = LocalDate.now().format(formatter);
        String fileName = "report_" + date + ".txt";
        Path path = Paths.get(fileName);
        StringBuilder sb = new StringBuilder();
        // report header
        sb.append("Library Report - ").append(date).append(System.lineSeparator()).append(System.lineSeparator());
        for (CollectionReport report : reports) {
            String header = "Collection: " + report.collectionName();
            sb.append(header).append(System.lineSeparator());
            sb.append("-".repeat(header.length())).append(System.lineSeparator());
            // table header
            String titleCol = "Title";
            String authorCol = "Author";
            sb.append(String.format("  %-30s | %s", titleCol, authorCol)).append(System.lineSeparator());
            sb.append(String.format("  %s | %s", "-".repeat(30), "-".repeat(authorCol.length()))).append(System.lineSeparator());
            // entries
            for (var book : report.books()) {
                sb.append(String.format("  %-30s | %s", book.title(), book.authorName()))
                  .append(System.lineSeparator());
            }
            sb.append(System.lineSeparator());
        }
        try {
            Files.writeString(path, sb.toString());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write report file: " + fileName, e);
        }
    }
}

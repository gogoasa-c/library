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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReportingService
 *
 * <p>Implementation of the {@link ReportingInput} port. Responsibilities:
 * - Query collection, book and author persistence ports to gather domain data.
 * - Group books by collection and map domain books into presentation DTOs
 *   ({@link BookReport}, {@link CollectionReport}).
 * - Render a human-readable textual report and write it to disk.
 *
 * <p>Design notes:
 * - Returns an immutable list of CollectionReport (the in-memory model) and
 *   performs file-writing as a separate side-effect.
 * - Includes borrowed-date information for books when available.
 * - Uses small private helper methods for grouping, mapping and rendering to
 *   keep the public method concise and easy to test.
 */
public record ReportingService(CollectionPersistence collectionPersistence, BookPersistence bookPersistence,
                               AuthorPersistence authorPersistence) implements ReportingInput {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter BORROWED_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<CollectionReport> generateCollectionReports() {
        final List<Collection> collections = collectionPersistence.findAll();
        final Map<Long, List<Book>> booksByCollection = groupBooksByCollection(bookPersistence.findAll());
        final List<CollectionReport> reports = buildReports(collections, booksByCollection);

        final String date = LocalDate.now().format(DATE_FMT);
        final String fileName = "report_" + date + ".txt";
        final String content = renderReportText(reports, date);
        writeReportFile(fileName, content);

        return Collections.unmodifiableList(reports);
    }

    private Map<Long, List<Book>> groupBooksByCollection(final List<Book> books) {
        return books.stream().collect(Collectors.groupingBy(Book::collectionId));
    }

    private List<CollectionReport> buildReports(final List<Collection> collections,
                                                final Map<Long, List<Book>> booksByCollection) {
        return collections.stream()
            .map(col -> {
                final List<BookReport> bookReports = booksByCollection
                    .getOrDefault(col.id(), Collections.emptyList())
                    .stream()
                    .map(this::mapToBookReport)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
                return new CollectionReport(col.name(), bookReports);
            })
            .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    private BookReport mapToBookReport(final Book book) {
        final Author author = authorPersistence.findById(book.authorId())
            .orElseThrow(() -> new IllegalArgumentException("Author with ID " + book.authorId() + " does not exist."));

        final LocalDate borrowedAt = book.borrowedAt();
        final String titleWithBorrowInfo;
        if (borrowedAt == null) {
            titleWithBorrowInfo = book.title();
        } else {
            titleWithBorrowInfo = String.format("%s (borrowed: %s)", book.title(), borrowedAt.format(BORROWED_DATE_FMT));
        }

        return new BookReport(titleWithBorrowInfo, author.name());
    }

    private String renderReportText(final List<CollectionReport> reports, final String date) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Library Report - ").append(date).append(System.lineSeparator()).append(System.lineSeparator());
        for (final CollectionReport report : reports) {
            final String header = "Collection: " + report.collectionName();
            sb.append(header).append(System.lineSeparator());
            sb.append(repeat('-', header.length())).append(System.lineSeparator());

            final String titleCol = "Title";
            final String authorCol = "Author";
            sb.append(String.format("  %-30s | %s", titleCol, authorCol)).append(System.lineSeparator());
            sb.append(String.format("  %-30s | %s", repeat('-', 30), repeat('-', authorCol.length()))).append(System.lineSeparator());

            for (final BookReport book : report.books()) {
                sb.append(String.format("  %-30s | %s", book.title(), book.authorName()))
                  .append(System.lineSeparator());
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    private void writeReportFile(final String fileName, final String content) {
        final Path path = Paths.get(fileName);
        try {
            Files.writeString(path, content);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to write report file: " + fileName, e);
        }
    }

    private String repeat(final char c, final int count) {
        if (count <= 0) return "";
        final char[] arr = new char[count];
        Arrays.fill(arr, c);
        return new String(arr);
    }
}

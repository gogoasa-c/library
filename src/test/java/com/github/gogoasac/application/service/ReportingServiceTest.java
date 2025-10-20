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
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ReportingService")
class ReportingServiceTest {

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    Path reportPath;
    ReportingInput service;
    Clock fixedClock;
    String fixedDate;

    @Nested
    @DisplayName("generateCollectionReports")
    class GenerateCollectionReportsTests {
        CollectionPersistence colPersist;
        BookPersistence bookPersist;
        AuthorPersistence authPersist;
        List<Collection> colList;
        List<Book> bookList;
        Map<Long, Author> authors;

        @BeforeEach
        void setup() throws IOException {
            // Use a fixed clock for deterministic testing
            fixedClock = Clock.fixed(Instant.parse("2025-10-20T12:00:00Z"), ZoneId.systemDefault());
            fixedDate = LocalDate.now(fixedClock).format(fmt);
            reportPath = Path.of("report_" + fixedDate + ".txt");
            Files.deleteIfExists(reportPath);

            colList = List.of(
                new Collection(1L, "Sci-Fi"),
                new Collection(2L, "Fantasy")
            );
            colPersist = new CollectionPersistence() {
                @Override
                public Collection addCollection(Collection collection) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Collection> findById(Long id) {
                    return colList.stream().filter(c -> c.id().equals(id)).findFirst();
                }

                @Override
                public List<Collection> findAll() {
                    return colList;
                }
            };

            // stub books
            bookList = List.of(
                new Book(1L, "Dune", 1L, 1L, 1965),
                new Book(2L, "Neuromancer", 2L, 1L, 1984),
                new Book(3L, "LOTR", 3L, 2L, 1954)
            );
            bookPersist = new BookPersistence() {
                @Override
                public Book addBook(Book book) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Book> findById(Long id) {
                    return bookList.stream().filter(b -> b.id().equals(id)).findFirst();
                }

                @Override
                public List<Book> findAll() {
                    return bookList;
                }
            };

            authors = Map.of(
                1L, new Author(1L, "Frank Herbert"),
                2L, new Author(2L, "William Gibson"),
                3L, new Author(3L, "J.R.R. Tolkien")
            );
            authPersist = new AuthorPersistence() {
                @Override
                public Author addAuthor(Author author) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Author> findById(Long id) {
                    return Optional.ofNullable(authors.get(id));
                }

                @Override
                public List<Author> findAll() {
                    return new ArrayList<>(authors.values());
                }
            };

            service = new ReportingService(colPersist, bookPersist, authPersist, fixedClock);
        }

        @Test
        @DisplayName("should return correct collection reports from data")
        void shouldReturnReports() {
            List<CollectionReport> reports = service.generateCollectionReports();
            assertEquals(2, reports.size(), "Should have two collection reports");

            CollectionReport sciFiReport = reports.stream()
                .filter(r -> r.collectionName().equals("Sci-Fi")).findFirst().orElseThrow();
            List<BookReport> sciBooks = sciFiReport.books();
            assertEquals(2, sciBooks.size());
            Map<String, String> titleToAuthor = sciBooks.stream()
                .collect(Collectors.toMap(BookReport::title, BookReport::authorName));
            assertEquals("Frank Herbert", titleToAuthor.get("Dune"));
            assertEquals("William Gibson", titleToAuthor.get("Neuromancer"));
        }

        @Test
        @DisplayName("should write a pretty text file with proper headers and entries")
        void shouldWriteReportFile() throws IOException {
            service.generateCollectionReports();
            assertTrue(Files.exists(reportPath), "Report file should be created");

            List<String> lines = Files.readAllLines(reportPath);
            assertTrue(lines.get(0).contains("Library Report - " + fixedDate));
            assertTrue(lines.contains("Collection: Sci-Fi"));
            assertTrue(lines.contains("Collection: Fantasy"));
            assertTrue(lines.stream().anyMatch(l -> l.trim().startsWith("Title") && l.contains("| Author")));
            assertTrue(lines.stream().anyMatch(l -> l.contains("Dune") && l.contains("Frank Herbert")));
            assertTrue(lines.stream().anyMatch(l -> l.contains("LOTR") && l.contains("J.R.R. Tolkien")));
        }

        @AfterEach
        void cleanup() throws IOException {
            Files.deleteIfExists(reportPath);
        }
    }
}
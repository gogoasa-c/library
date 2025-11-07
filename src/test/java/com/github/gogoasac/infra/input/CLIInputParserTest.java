package com.github.gogoasac.infra.input;

import com.github.gogoasac.application.dto.*;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.application.input.ReportingInput;
import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.domain.entity.Book;
import com.github.gogoasac.domain.entity.Collection;
import com.github.gogoasac.infra.input.menu.AuthorMenu;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CLIInputParser Tests")
class CLIInputParserTest {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private Path reportPath;

    @BeforeEach
    void beforeEach() throws IOException {
        reportPath = Paths.get("report_" + LocalDate.now().format(DATE_FMT) + ".txt");
        Files.deleteIfExists(reportPath);
    }

    @AfterEach
    void afterEach() throws IOException {
        Files.deleteIfExists(reportPath);
    }

    /* Helper that runs the parser with injected mocks and returns captured stdout */
    private String runWithInput(String input,
                                AuthorManagementInput authorInput,
                                BookManagementInput bookInput,
                                CollectionManagementInput collectionInput,
                                ReportingInput reportingInput) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos, true)) {
            InputStream in = new ByteArrayInputStream(input.getBytes());
            final AuthorMenu authorMenu = new AuthorMenu(ps, in, authorInput);
            CLIInputParser parser = new CLIInputParser(authorInput, bookInput, collectionInput, reportingInput, in, ps,
                list -> {}, authorMenu);
            parser.run();
        }
        return baos.toString();
    }

    @Test
    @DisplayName("Exit immediately")
    void exitImmediately() {
        // simple no-op mocks
        AuthorManagementInput a = new NoopAuthorInput();
        BookManagementInput b = new NoopBookInput();
        CollectionManagementInput c = new NoopCollectionInput();
        ReportingInput r = List::of;

        String out = runWithInput("0\n", a, b, c, r);

        assertTrue(out.contains("Welcome to the Library TUI"));
        assertTrue(out.contains("Exiting. Goodbye!"));
    }

    @Test
    @DisplayName("Add author and list")
    void addAuthorAndList() {
        MutableAuthorInput authorInput = new MutableAuthorInput();
        BookManagementInput bookInput = new NoopBookInput();
        CollectionManagementInput collectionInput = new NoopCollectionInput();
        ReportingInput reporting = List::of;

        // menu sequence: Authors (1) -> Add (1) -> name -> List (2) -> Back (9) -> Exit (0)
        String input = String.join("\n", "1", "1", "Test Author", "2", "9", "0") + "\n";
        String out = runWithInput(input, authorInput, bookInput, collectionInput, reporting);

        assertTrue(out.contains("Author created"), "Should report creation");
        assertTrue(out.contains("Test Author"), "Should include author name in output");
        assertTrue(out.contains("Authors:"), "Should list authors");
        // ensure persistence-like behavior in the mock
        List<Author> authors = authorInput.getAll();
        assertEquals(1, authors.size());
        assertEquals("Test Author", authors.getFirst().name());
    }

    @Test
    @DisplayName("Add collection and list")
    void addCollectionAndList() {
        AuthorManagementInput authorInput = new NoopAuthorInput();
        BookManagementInput bookInput = new NoopBookInput();
        MutableCollectionInput collectionInput = new MutableCollectionInput();
        ReportingInput reporting = List::of;

        String input = String.join("\n", "2", "1", "My Collection", "2", "9", "0") + "\n";
        String out = runWithInput(input, authorInput, bookInput, collectionInput, reporting);

        assertTrue(out.contains("Collection created"));
        assertTrue(out.contains("My Collection"));
        assertTrue(out.contains("Collections:"));
        assertEquals(1, collectionInput.getAll().size());
    }

    @Test
    @DisplayName("Add book flow and list books shows author/collection names")
    void addBookFlow() {
        MutableAuthorInput authorInput = new MutableAuthorInput();
        MutableCollectionInput collectionInput = new MutableCollectionInput();
        MutableBookInput bookInput = new MutableBookInput(authorInput, collectionInput);
        ReportingInput reporting = List::of;

        // Sequence:
        // 1 (Authors) -> 1 (Add) -> "Author A" -> 9 (Back)
        // 2 (Collections) -> 1 (Add) -> "Coll A" -> 9 (Back)
        // 3 (Books) -> 1 (Add) -> title, authorId, collectionId, year -> 2 (List) -> 9 -> 0
        String input = String.join("\n",
            "1", "1", "Author A", "9",
            "2", "1", "Coll A", "9",
            "3", "1",
            "Great Book",
            "1",   // author id
            "1",   // collection id
            "2025",
            "2", "9",
            "0") + "\n";

        String out = runWithInput(input, authorInput, bookInput, collectionInput, reporting);

        assertTrue(out.contains("Book created"), "Should report book creation");
        assertTrue(out.contains("Great Book"), "Should include book title");
        // list should show resolved author/collection strings
        assertTrue(out.contains("Author A"), "Should show author name in listing");
        assertTrue(out.contains("Coll A"), "Should show collection name in listing");

        // Ensure persisted-like state in mocks
        List<Book> books = bookInput.getAll();
        assertEquals(1, books.size());
        Book saved = books.getFirst();
        assertEquals("Great Book", saved.title());
        assertEquals(1L, saved.authorId());
        assertEquals(1L, saved.collectionId());
    }

    @Test
    @DisplayName("Invalid number input handling")
    void invalidNumberInput() {
        AuthorManagementInput a = new NoopAuthorInput();
        BookManagementInput b = new NoopBookInput();
        CollectionManagementInput c = new NoopCollectionInput();
        ReportingInput r = List::of;

        // Books -> View by id -> 'abc' -> Back -> Exit
        String input = String.join("\n", "3", "3", "abc", "9", "0") + "\n";
        String out = runWithInput(input, a, b, c, r);

        assertTrue(out.contains("Invalid number: 'abc'"));
    }

    @Test
    @DisplayName("Generate report writes file")
    void generateReportWritesFile() throws Exception {
        // reporting mock writes the expected file
        ReportingInput reporting = () -> {
            // write a minimal report file to emulate real behaviour
            String header = "Library Report - " + LocalDate.now().format(DATE_FMT);
            List<String> lines = List.of(header, "Collection: RCol", "Book: Reported Book");
            try {
                Files.write(reportPath, lines);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return List.of(new CollectionReport("Dummy name",
                List.of(new BookReport("Dummy title", "Dummy author name"))));
        };

        AuthorManagementInput a = new NoopAuthorInput();
        BookManagementInput b = new NoopBookInput();
        CollectionManagementInput c = new NoopCollectionInput();

        String input = String.join("\n", "4", "n", "0") + "\n";
        String out = runWithInput(input, a, b, c, reporting);

        assertTrue(out.contains("Generating collection reports"), "Should indicate generation");
        assertTrue(Files.exists(reportPath), "Report file should exist");
        String content = Files.readString(reportPath);
        assertTrue(content.contains("Library Report"), "Report should contain header");
    }

    // --- Lightweight mock implementations used in tests ---

    private static class NoopAuthorInput implements AuthorManagementInput {
        @Override
        public Author addAuthor(AddAuthorCommand cmd) {
            return null;
        }

        @Override
        public Author getById(Long id) {
            return null;
        }

        @Override
        public List<Author> getAll() {
            return List.of();
        }
    }

    private static class NoopCollectionInput implements CollectionManagementInput {
        @Override
        public Collection addCollection(AddCollectionCommand cmd) {
            return null;
        }

        @Override
        public Collection getById(Long id) {
            return null;
        }

        @Override
        public List<Collection> getAll() {
            return List.of();
        }
    }

    private static class NoopBookInput implements BookManagementInput {
        @Override
        public Book addBook(AddBookCommand cmd) {
            return null;
        }

        @Override
        public Book getById(Long id) {
            return null;
        }

        @Override
        public List<Book> getAll() {
            return List.of();
        }
    }

    private static class MutableAuthorInput implements AuthorManagementInput {
        private long nextId = 1L;
        private final List<Author> list = new ArrayList<>();

        @Override
        public Author addAuthor(AddAuthorCommand cmd) {
            Author a = new Author(nextId++, cmd.name());
            list.add(a);
            return a;
        }

        @Override
        public Author getById(Long id) {
            return list.stream().filter(x -> x.id().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Author> getAll() {
            return List.copyOf(list);
        }
    }

    private static class MutableCollectionInput implements CollectionManagementInput {
        private long nextId = 1L;
        private final List<Collection> list = new ArrayList<>();

        @Override
        public Collection addCollection(AddCollectionCommand cmd) {
            Collection c = new Collection(nextId++, cmd.name());
            list.add(c);
            return c;
        }

        @Override
        public Collection getById(Long id) {
            return list.stream().filter(x -> x.id().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Collection> getAll() {
            return List.copyOf(list);
        }
    }

    private static class MutableBookInput implements BookManagementInput {
        private long nextId = 1L;
        private final List<Book> list = new ArrayList<>();
        private final MutableAuthorInput authors;
        private final MutableCollectionInput collections;

        MutableBookInput(MutableAuthorInput authors, MutableCollectionInput collections) {
            this.authors = authors;
            this.collections = collections;
        }

        @Override
        public Book addBook(AddBookCommand cmd) {
            // basic validation emulated
            if (authors.getById(cmd.authorId()) == null || collections.getById(cmd.collectionId()) == null) {
                throw new IllegalArgumentException("Author or collection not found");
            }
            Book b = new Book(nextId++, cmd.title(), cmd.authorId(), cmd.collectionId(), cmd.publicationYear());
            list.add(b);
            return b;
        }

        @Override
        public Book getById(Long id) {
            return list.stream().filter(x -> x.id().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Book> getAll() {
            return List.copyOf(list);
        }
    }
}


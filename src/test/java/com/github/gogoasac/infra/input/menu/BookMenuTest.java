package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.input.BookManagementInput;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.domain.entity.Book;
import com.github.gogoasac.domain.entity.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookMenu Tests")
class BookMenuTest {

    private ByteArrayOutputStream outputBuffer;
    private PrintStream printStream;

    @BeforeEach
    void setUpStreams() {
        outputBuffer = new ByteArrayOutputStream();
        printStream = new PrintStream(outputBuffer, true, StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("Add and list flows")
    class AddAndList {

        @Test
        void shouldAddBookAndThenListIt() {
            /*
              Input sequence for BookMenu.run:
              1 -> Add book
              Title
              authorId
              collectionId
              year
              2 -> List books
              9 -> Back
             */
            final MutableAuthorInput authorInput = new MutableAuthorInput();
            final Author createdAuthor = authorInput.addAuthor(new AddAuthorCommand("Author X"));

            final MutableCollectionInput collectionInput = new MutableCollectionInput();
            final Collection createdCollection = collectionInput.addCollection(new AddCollectionCommand("Coll X"));

            final MutableBookInput bookInput = new MutableBookInput(authorInput, collectionInput);

            final String inputLines = String.join("\n",
                "1",
                "Great Book",
                String.valueOf(createdAuthor.id()),
                String.valueOf(createdCollection.id()),
                "2025",
                "2",
                "9") + "\n";

            final BufferedReader sharedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

            final BookMenu menu = new BookMenu(printStream, sharedReader, bookInput, authorInput, collectionInput);
            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Book created"), "Should report creation");
            assertTrue(output.contains("Great Book"), "Should include book title");
            assertTrue(output.contains("Author X"), "Should include author name in listing");
            assertTrue(output.contains("Coll X"), "Should include collection name in listing");

            final List<Book> persisted = bookInput.getAll();
            assertEquals(1, persisted.size(), "One book persisted");
            assertEquals("Great Book", persisted.getFirst().title());
        }
    }

    @Nested
    @DisplayName("View by id flows")
    class ViewById {

        @Test
        void shouldViewBookByIdWhenExists() {
            final MutableAuthorInput authorInput = new MutableAuthorInput();
            final Author createdAuthor = authorInput.addAuthor(new AddAuthorCommand("Author Y"));

            final MutableCollectionInput collectionInput = new MutableCollectionInput();
            final Collection createdCollection = collectionInput.addCollection(new AddCollectionCommand("Coll Y"));

            final MutableBookInput bookInput = new MutableBookInput(authorInput, collectionInput);
            final Book createdBook = bookInput.addBook(new AddBookCommand("Book Y", createdAuthor.id(), createdCollection.id(), 2020));

            final String inputLines = String.join("\n", "3", String.valueOf(createdBook.id()), "9") + "\n";
            final BufferedReader sharedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            final BookMenu menu = new BookMenu(printStream, sharedReader, bookInput, authorInput, collectionInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Book:"), "Should print book header");
            assertTrue(output.contains("Book Y"), "Should include the book title");
        }

        @Test
        void shouldReportInvalidNumberWhenNonNumericIdProvided() {
            final MutableAuthorInput authorInput = new MutableAuthorInput();
            final MutableCollectionInput collectionInput = new MutableCollectionInput();
            final MutableBookInput bookInput = new MutableBookInput(authorInput, collectionInput);

            final String inputLines = String.join("\n", "3", "abc", "9") + "\n";
            final BufferedReader sharedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            final BookMenu menu = new BookMenu(printStream, sharedReader, bookInput, authorInput, collectionInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Invalid number") || output.contains("Invalid number: 'abc'"),
                "Should report invalid number parsing");
        }
    }

    @Nested
    @DisplayName("Validation and error handling")
    class ValidationAndErrors {

        @Test
        void shouldRejectEmptyTitleWhenAddingBook() {
            final MutableAuthorInput authorInput = new MutableAuthorInput();
            final MutableCollectionInput collectionInput = new MutableCollectionInput();
            final MutableBookInput bookInput = new MutableBookInput(authorInput, collectionInput);

            final String inputLines = String.join("\n", "1", "", "9") + "\n";
            final BufferedReader sharedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            final BookMenu menu = new BookMenu(printStream, sharedReader, bookInput, authorInput, collectionInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Title cannot be empty"), "Should reject empty title");
            assertEquals(0, bookInput.getAll().size(), "No book should be persisted");
        }
    }

    // --- lightweight in-memory mocks used by tests ---
    private static final class MutableAuthorInput implements AuthorManagementInput {
        private long nextId = 1L;
        private final List<Author> storage = new ArrayList<>();

        @Override
        public Author addAuthor(AddAuthorCommand cmd) {
            final Author created = new Author(nextId++, cmd.name());
            storage.add(created);
            return created;
        }

        @Override
        public Author getById(Long id) {
            return storage.stream().filter(a -> a.id().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Author> getAll() {
            return List.copyOf(storage);
        }
    }

    private static final class MutableCollectionInput implements CollectionManagementInput {
        private long nextId = 1L;
        private final List<Collection> storage = new ArrayList<>();

        @Override
        public Collection addCollection(AddCollectionCommand cmd) {
            final Collection created = new Collection(nextId++, cmd.name());
            storage.add(created);
            return created;
        }

        @Override
        public Collection getById(Long id) {
            return storage.stream().filter(c -> c.id().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Collection> getAll() {
            return List.copyOf(storage);
        }
    }

    private static final class MutableBookInput implements BookManagementInput {
        private long nextId = 1L;
        private final List<Book> storage = new ArrayList<>();
        private final MutableAuthorInput authors;
        private final MutableCollectionInput collections;

        MutableBookInput(MutableAuthorInput authors, MutableCollectionInput collections) {
            this.authors = authors;
            this.collections = collections;
        }

        @Override
        public Book addBook(AddBookCommand cmd) {
            if (authors.getById(cmd.authorId()) == null || collections.getById(cmd.collectionId()) == null) {
                throw new IllegalArgumentException("Author or collection not found");
            }
            final Book created = new Book(nextId++, cmd.title(), cmd.authorId(), cmd.collectionId(), cmd.publicationYear());
            storage.add(created);
            return created;
        }

        @Override
        public Book getById(Long id) {
            return storage.stream().filter(b -> b.id().equals(id)).findFirst().orElse(null);
        }

        @Override
        public List<Book> getAll() {
            return List.copyOf(storage);
        }

        @Override
        public void borrow(final Long bookId) {
            if (bookId == null) {
                throw new IllegalArgumentException("bookId required");
            }
            for (int i = 0; i < storage.size(); i++) {
                final Book current = storage.get(i);
                if (current.id().equals(bookId)) {
                    // use domain convenience method if present, otherwise set manually
                    Book updated;
                    try {
                        updated = current.borrow();
                    } catch (UnsupportedOperationException | IllegalStateException ex) {
                        // fallback: create a new Book with borrowed flag true if record supports the fields
                        updated = new Book(current.id(), current.title(), current.authorId(), current.collectionId(), current.publicationYear(), java.time.LocalDate.now(), true);
                    }
                    storage.set(i, updated);
                    return;
                }
            }
            throw new IllegalArgumentException("Book not found: " + bookId);
        }
    }
}
package com.github.gogoasac.infra.output;

import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.domain.entity.Book;
import com.github.gogoasac.domain.entity.Collection;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookRepository Tests")
class BookRepositoryTest {
    private static final String FILE_PATH = "Books_test.json";
    private BookRepository repository;
    private Author testAuthor;
    private Collection testCollection;

    @BeforeEach
    void setUp() {
        repository = new BookRepository(FILE_PATH);
        testAuthor = new Author(1L, "Test Author");
        testCollection = new Collection(1L, "Test Collection");
    }

    @AfterEach
    void tearDown() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            boolean deleted = file.delete();
            assertTrue(deleted, "Failed to delete test file " + FILE_PATH);
        }
    }

    @Nested
    @DisplayName("addBook method tests")
    class AddBookTests {
        @Test
        @DisplayName("Should create new book with generated ID")
        void addBook_ShouldCreateNewBookWithGeneratedId() {
            Book book = new Book(null, "Test Book", testAuthor.id(), testCollection.id(), 2024);

            Book savedBook = repository.addBook(book);

            assertNotNull(savedBook.id());
            assertEquals(book.title(), savedBook.title());
            assertEquals(book.authorId(), savedBook.authorId());
            assertEquals(book.collectionId(), savedBook.collectionId());
            assertEquals(book.publicationYear(), savedBook.publicationYear());
        }

        @Test
        @DisplayName("Should generate unique IDs for multiple books")
        void addMultipleBooks_ShouldGenerateUniqueIds() {
            Book book1 = new Book(null, "Book 1", testAuthor.id(), testCollection.id(), 2024);
            Book book2 = new Book(null, "Book 2", testAuthor.id(), testCollection.id(), 2024);

            Book savedBook1 = repository.addBook(book1);
            Book savedBook2 = repository.addBook(book2);

            assertNotEquals(savedBook1.id(), savedBook2.id());
        }
    }

    @Nested
    @DisplayName("findById method tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return book when exists")
        void findById_WhenBookExists_ShouldReturnBook() {
            Book book = repository.addBook(
                new Book(null, "Test Book", testAuthor.id(), testCollection.id(), 2024)
            );

            Optional<Book> found = repository.findById(book.id());

            assertTrue(found.isPresent());
            assertEquals(book.title(), found.get().title());
            assertEquals(book.authorId(), found.get().authorId());
            assertEquals(book.collectionId(), found.get().collectionId());
        }

        @Test
        @DisplayName("Should return empty when book doesn't exist")
        void findById_WhenBookDoesNotExist_ShouldReturnEmpty() {
            Optional<Book> found = repository.findById(999L);

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findAll method tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all books when books exist")
        void findAll_WhenBooksExist_ShouldReturnAllBooks() {
            repository.addBook(
                new Book(null, "Book 1", testAuthor.id(), testCollection.id(), 2024)
            );
            repository.addBook(
                new Book(null, "Book 2", testAuthor.id(), testCollection.id(), 2024)
            );

            List<Book> books = repository.findAll();

            assertEquals(2, books.size());
            assertTrue(books.stream().anyMatch(b -> b.title().equals("Book 1")));
            assertTrue(books.stream().anyMatch(b -> b.title().equals("Book 2")));
        }

        @Test
        @DisplayName("Should return empty list when no books exist")
        void findAll_WhenNoBooks_ShouldReturnEmptyList() {
            List<Book> books = repository.findAll();

            assertTrue(books.isEmpty());
        }
    }

    @Nested
    @DisplayName("Persistence behavior tests")
    class PersistenceTests {
        @Test
        @DisplayName("Should persist data between repository instances")
        void persistenceTest_ShouldPersistBetweenRepositoryInstances() {
            Book book = repository.addBook(
                new Book(null, "Persistent Book", testAuthor.id(), testCollection.id(), 2024)
            );

            BookRepository newRepository = new BookRepository(FILE_PATH);
            Optional<Book> found = newRepository.findById(book.id());

            assertTrue(found.isPresent());
            assertEquals(book.title(), found.get().title());
        }

        @Test
        @DisplayName("Should preserve book field values across repository instances")
        void persistenceTest_ShouldPreserveBookFields() {
            // create book with specific field values
            Long authorId = 42L;
            Long collectionId = 24L;
            Book book = repository.addBook(
                new Book(null, "Complex Book", authorId, collectionId, 2024)
            );

            BookRepository newRepository = new BookRepository(FILE_PATH);
            Optional<Book> found = newRepository.findById(book.id());

            assertTrue(found.isPresent());
            // verify all fields are persisted
            Book retrieved = found.get();
            assertEquals(book.title(), retrieved.title());
            assertEquals(book.authorId(), retrieved.authorId());
            assertEquals(book.collectionId(), retrieved.collectionId());
            assertEquals(book.publicationYear(), retrieved.publicationYear());
        }
    }
}

package com.github.gogoasac.infra.output;

import com.github.gogoasac.domain.entity.Author;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorRepository Tests")
class AuthorRepositoryTest {
    private static final String FILE_PATH = "Authors_test.json";
    private AuthorRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AuthorRepository(FILE_PATH);
    }

    @AfterEach
    void tearDown() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Nested
    @DisplayName("addAuthor method tests")
    class AddAuthorTests {
        @Test
        @DisplayName("Should create new author with generated ID")
        void addAuthor_ShouldCreateNewAuthorWithGeneratedId() {
            Author author = new Author(null, "Test Author");

            Author savedAuthor = repository.addAuthor(author);

            assertNotNull(savedAuthor.id());
            assertEquals(author.name(), savedAuthor.name());
        }

        @Test
        @DisplayName("Should generate unique IDs for multiple authors")
        void addMultipleAuthors_ShouldGenerateUniqueIds() {
            Author author1 = new Author(null, "Author 1");
            Author author2 = new Author(null, "Author 2");

            Author savedAuthor1 = repository.addAuthor(author1);
            Author savedAuthor2 = repository.addAuthor(author2);

            assertNotEquals(savedAuthor1.id(), savedAuthor2.id());
        }

        @Test
        @DisplayName("Should create distinct entries for authors with same name")
        void addAuthor_WithSameName_ShouldCreateDistinctEntries() {
            String authorName = "Duplicate Name";

            Author author1 = repository.addAuthor(new Author(null, authorName));
            Author author2 = repository.addAuthor(new Author(null, authorName));

            assertNotEquals(author1.id(), author2.id());
            List<Author> authors = repository.findAll();
            assertEquals(2, authors.stream()
                .filter(a -> a.name().equals(authorName))
                .count());
        }
    }

    @Nested
    @DisplayName("findById method tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return author when exists")
        void findById_WhenAuthorExists_ShouldReturnAuthor() {
            Author author = repository.addAuthor(new Author(null, "Test Author"));

            Optional<Author> found = repository.findById(author.id());

            assertTrue(found.isPresent());
            assertEquals(author.name(), found.get().name());
        }

        @Test
        @DisplayName("Should return empty when author doesn't exist")
        void findById_WhenAuthorDoesNotExist_ShouldReturnEmpty() {
            Optional<Author> found = repository.findById(999L);

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findAll method tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all authors when authors exist")
        void findAll_WhenAuthorsExist_ShouldReturnAllAuthors() {
            repository.addAuthor(new Author(null, "Author 1"));
            repository.addAuthor(new Author(null, "Author 2"));

            List<Author> authors = repository.findAll();

            assertEquals(2, authors.size());
            assertTrue(authors.stream().anyMatch(a -> a.name().equals("Author 1")));
            assertTrue(authors.stream().anyMatch(a -> a.name().equals("Author 2")));
        }

        @Test
        @DisplayName("Should return empty list when no authors exist")
        void findAll_WhenNoAuthors_ShouldReturnEmptyList() {
            List<Author> authors = repository.findAll();

            assertTrue(authors.isEmpty());
        }
    }

    @Nested
    @DisplayName("Persistence behavior tests")
    class PersistenceTests {
        @Test
        @DisplayName("Should persist data between repository instances")
        void persistenceTest_ShouldPersistBetweenRepositoryInstances() {
            Author author = repository.addAuthor(new Author(null, "Persistent Author"));

            AuthorRepository newRepository = new AuthorRepository(FILE_PATH);
            Optional<Author> found = newRepository.findById(author.id());

            assertTrue(found.isPresent());
            assertEquals(author.name(), found.get().name());
        }
    }
}

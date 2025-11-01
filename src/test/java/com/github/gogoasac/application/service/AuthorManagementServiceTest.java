package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.AddAuthorCommand;
import com.github.gogoasac.application.input.AuthorManagementInput;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.domain.entity.Author;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorManagementService")
class AuthorManagementServiceTest {

    private AuthorPersistence authorPersistence;
    private AuthorManagementInput service;

    @Nested
    @DisplayName("addAuthor")
    class AddAuthorTests {
        @BeforeEach
        void setup() {
            authorPersistence = new AuthorPersistence() {
                private Long nextId = 1L;

                @Override
                public Author addAuthor(Author author) {
                    return new Author(nextId++, author.name());
                }

                @Override
                public Optional<Author> findById(Long id) {
                    return Optional.empty();
                }

                @Override
                public List<Author> findAll() {
                    return List.of();
                }
            };
            service = new AuthorManagementService(authorPersistence);
        }

        @Test
        @DisplayName("should add author and return it with generated ID")
        void shouldAddAuthor() {
            AddAuthorCommand command = new AddAuthorCommand("Jane Austen");
            Author result = service.addAuthor(command);

            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("Jane Austen", result.name());
        }

        @Test
        @DisplayName("should assign sequential IDs to multiple authors")
        void shouldAssignSequentialIds() {
            Author author1 = service.addAuthor(new AddAuthorCommand("Author One"));
            Author author2 = service.addAuthor(new AddAuthorCommand("Author Two"));

            assertNotNull(author1.id());
            assertNotNull(author2.id());
            assertNotEquals(author1.id(), author2.id());
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAllAuthorsTests {
        @BeforeEach
        void setup() {
            List<Author> authors = List.of(
                new Author(1L, "Author A"),
                new Author(2L, "Author B")
            );

            authorPersistence = new AuthorPersistence() {
                @Override
                public Author addAuthor(Author author) {
                    return null;
                }

                @Override
                public Optional<Author> findById(Long id) {
                    return authors.stream().filter(a -> a.id().equals(id)).findFirst();
                }

                @Override
                public List<Author> findAll() {
                    return authors;
                }
            };
            service = new AuthorManagementService(authorPersistence);
        }

        @Test
        @DisplayName("should return all authors")
        void shouldReturnAllAuthors() {
            List<Author> result = service.getAll();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(a -> a.name().equals("Author A")));
            assertTrue(result.stream().anyMatch(a -> a.name().equals("Author B")));
        }

        @Test
        @DisplayName("should return empty list when no authors exist")
        void shouldReturnEmptyList() {
            authorPersistence = new AuthorPersistence() {
                @Override
                public Author addAuthor(Author author) {
                    return null;
                }

                @Override
                public Optional<Author> findById(Long id) {
                    return Optional.empty();
                }

                @Override
                public List<Author> findAll() {
                    return List.of();
                }
            };
            service = new AuthorManagementService(authorPersistence);

            List<Author> result = service.getAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {
        @BeforeEach
        void setup() {
            Author existingAuthor = new Author(1L, "Existing Author");

            authorPersistence = new AuthorPersistence() {
                @Override
                public Author addAuthor(Author author) {
                    return null;
                }

                @Override
                public Optional<Author> findById(Long id) {
                    return id.equals(1L) ? Optional.of(existingAuthor) : Optional.empty();
                }

                @Override
                public List<Author> findAll() {
                    return List.of(existingAuthor);
                }
            };
            service = new AuthorManagementService(authorPersistence);
        }

        @Test
        @DisplayName("should return author when ID exists")
        void shouldReturnAuthorWhenExists() {
            Author result = service.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("Existing Author", result.name());
        }

        @Test
        @DisplayName("should throw exception when ID does not exist")
        void shouldThrowWhenIdNotExists() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(999L)
            );

            assertTrue(exception.getMessage().contains("Author with ID 999 does not exist"));
        }
    }
}
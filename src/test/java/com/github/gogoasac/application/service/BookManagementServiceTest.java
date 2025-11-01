package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.AddBookCommand;
import com.github.gogoasac.application.output.AuthorPersistence;
import com.github.gogoasac.application.output.BookPersistence;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Author;
import com.github.gogoasac.domain.entity.Book;
import com.github.gogoasac.domain.entity.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookManagementService")
class BookManagementServiceTest {

    BookPersistence bookPersist;
    AuthorPersistence authorPersist;
    CollectionPersistence collectionPersist;
    BookManagementService service;

    @Nested
    @DisplayName("addBook")
    class AddBookTests {
        @BeforeEach
        void setup() {
            authorPersist = new AuthorPersistence() {
                @Override
                public Author addAuthor(Author author) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Author> findById(Long id) {
                    return id != null && id == 1L ? Optional.of(new Author(1L, "Author One")) : Optional.empty();
                }

                @Override
                public List<Author> findAll() {
                    return Collections.emptyList();
                }
            };

            collectionPersist = new CollectionPersistence() {
                @Override
                public Collection addCollection(Collection collection) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Collection> findById(Long id) {
                    return id != null && id == 10L ? Optional.of(new Collection(10L, "Collection Ten")) : Optional.empty();
                }

                @Override
                public List<Collection> findAll() {
                    return Collections.emptyList();
                }
            };

            bookPersist = new BookPersistence() {
                private long nextId = 1L;
                private final List<Book> storage = new ArrayList<>();

                @Override
                public Book addBook(Book book) {
                    final Book saved = new Book(nextId++, book.title(), book.authorId(), book.collectionId(), book.publicationYear());
                    storage.add(saved);
                    return saved;
                }

                @Override
                public Optional<Book> findById(Long id) {
                    return storage.stream().filter(b -> b.id().equals(id)).findFirst();
                }

                @Override
                public List<Book> findAll() {
                    return Collections.unmodifiableList(new ArrayList<>(storage));
                }
            };

            service = new BookManagementService(bookPersist, authorPersist, collectionPersist);
        }

        @Test
        @DisplayName("should add book when author and collection exist")
        void shouldAddBook() {
            AddBookCommand cmd = new AddBookCommand("New Title", 1L, 10L, 2021);
            Book result = service.addBook(cmd);

            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("New Title", result.title());
            assertEquals(1L, result.authorId());
            assertEquals(10L, result.collectionId());
            assertEquals(2021, result.publicationYear());

            List<Book> all = bookPersist.findAll();
            assertEquals(1, all.size());
            assertEquals(result.id(), all.getFirst().id());
        }

        @Test
        @DisplayName("should throw when author does not exist")
        void shouldThrowWhenAuthorMissing() {
            AddBookCommand cmd = new AddBookCommand("Bad Book", 99L, 10L, 2021);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addBook(cmd));
            assertTrue(ex.getMessage().contains("Author"));
        }

        @Test
        @DisplayName("should throw when collection does not exist")
        void shouldThrowWhenCollectionMissing() {
            AddBookCommand cmd = new AddBookCommand("Bad Book", 1L, 999L, 2021);
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.addBook(cmd));
            assertTrue(ex.getMessage().contains("Collection"));
        }
    }

    @Nested
    @DisplayName("getAll and getById")
    class RetrievalTests {
        @BeforeEach
        void setup() {
            final List<Book> initial = Arrays.asList(
                new Book(1L, "Alpha", 1L, 10L, 2000),
                new Book(2L, "Beta", 1L, 10L, 2001)
            );

            bookPersist = new BookPersistence() {
                @Override
                public Book addBook(Book book) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Book> findById(Long id) {
                    return initial.stream().filter(b -> b.id().equals(id)).findFirst();
                }

                @Override
                public List<Book> findAll() {
                    return initial;
                }
            };

            authorPersist = new AuthorPersistence() {
                @Override
                public Author addAuthor(Author author) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Author> findById(Long id) {
                    return Optional.of(new Author(1L, "Author One"));
                }

                @Override
                public List<Author> findAll() {
                    return Collections.singletonList(new Author(1L, "Author One"));
                }
            };

            collectionPersist = new CollectionPersistence() {
                @Override
                public Collection addCollection(Collection collection) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Collection> findById(Long id) {
                    return Optional.of(new Collection(10L, "Collection Ten"));
                }

                @Override
                public List<Collection> findAll() {
                    return Collections.singletonList(new Collection(10L, "Collection Ten"));
                }
            };

            service = new BookManagementService(bookPersist, authorPersist, collectionPersist);
        }

        @Test
        @DisplayName("should return all books")
        void shouldReturnAll() {
            List<Book> all = service.getAll();
            assertNotNull(all);
            assertEquals(2, all.size());
            assertTrue(all.stream().anyMatch(b -> b.title().equals("Alpha")));
            assertTrue(all.stream().anyMatch(b -> b.title().equals("Beta")));
        }

        @Test
        @DisplayName("should return book by id when exists")
        void shouldReturnById() {
            Book b = service.getById(1L);
            assertNotNull(b);
            assertEquals(1L, b.id());
            assertEquals("Alpha", b.title());
        }

        @Test
        @DisplayName("should throw when getById not found")
        void shouldThrowWhenNotFound() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.getById(999L));
            assertTrue(ex.getMessage().contains("Book with ID 999"));
        }
    }
}
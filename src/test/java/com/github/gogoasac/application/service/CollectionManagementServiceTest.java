package com.github.gogoasac.application.service;

import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.application.input.CollectionManagementInput;
import com.github.gogoasac.application.output.CollectionPersistence;
import com.github.gogoasac.domain.entity.Collection;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CollectionManagementService")
class CollectionManagementServiceTest {

    private CollectionPersistence collectionPersistence;
    private CollectionManagementInput service;

    @Nested
    @DisplayName("addCollection")
    class AddCollectionTests {
        @BeforeEach
        void setup() {
            collectionPersistence = new CollectionPersistence() {
                private Long nextId = 1L;

                @Override
                public Collection save(Collection collection) {
                    return new Collection(nextId++, collection.name());
                }

                @Override
                public Optional<Collection> findById(Long id) {
                    return Optional.empty();
                }

                @Override
                public List<Collection> findAll() {
                    return List.of();
                }
            };
            service = new CollectionManagementService(collectionPersistence);
        }

        @Test
        @DisplayName("should add collection and return it with generated ID")
        void shouldAddCollection() {
            AddCollectionCommand command = new AddCollectionCommand("Science Fiction");
            Collection result = service.addCollection(command);

            assertNotNull(result);
            assertNotNull(result.id());
            assertEquals("Science Fiction", result.name());
        }

        @Test
        @DisplayName("should assign sequential IDs to multiple collections")
        void shouldAssignSequentialIds() {
            Collection col1 = service.addCollection(new AddCollectionCommand("Mystery"));
            Collection col2 = service.addCollection(new AddCollectionCommand("Romance"));

            assertNotNull(col1.id());
            assertNotNull(col2.id());
            assertNotEquals(col1.id(), col2.id());
        }
    }

    @Nested
    @DisplayName("getAll")
    class GetAllCollectionsTests {
        @BeforeEach
        void setup() {
            List<Collection> collections = List.of(
                new Collection(1L, "Fantasy"),
                new Collection(2L, "Horror")
            );

            collectionPersistence = new CollectionPersistence() {
                @Override
                public Collection save(Collection collection) {
                    return null;
                }

                @Override
                public Optional<Collection> findById(Long id) {
                    return collections.stream().filter(c -> c.id().equals(id)).findFirst();
                }

                @Override
                public List<Collection> findAll() {
                    return collections;
                }
            };
            service = new CollectionManagementService(collectionPersistence);
        }

        @Test
        @DisplayName("should return all collections")
        void shouldReturnAllCollections() {
            List<Collection> result = service.getAll();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(c -> c.name().equals("Fantasy")));
            assertTrue(result.stream().anyMatch(c -> c.name().equals("Horror")));
        }

        @Test
        @DisplayName("should return empty list when no collections exist")
        void shouldReturnEmptyList() {
            collectionPersistence = new CollectionPersistence() {
                @Override
                public Collection save(Collection collection) {
                    return null;
                }

                @Override
                public Optional<Collection> findById(Long id) {
                    return Optional.empty();
                }

                @Override
                public List<Collection> findAll() {
                    return List.of();
                }
            };
            service = new CollectionManagementService(collectionPersistence);

            List<Collection> result = service.getAll();
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getById")
    class GetByIdTests {
        @BeforeEach
        void setup() {
            Collection existingCollection = new Collection(1L, "Thriller");

            collectionPersistence = new CollectionPersistence() {
                @Override
                public Collection save(Collection collection) {
                    return null;
                }

                @Override
                public Optional<Collection> findById(Long id) {
                    return id.equals(1L) ? Optional.of(existingCollection) : Optional.empty();
                }

                @Override
                public List<Collection> findAll() {
                    return List.of(existingCollection);
                }
            };
            service = new CollectionManagementService(collectionPersistence);
        }

        @Test
        @DisplayName("should return collection when ID exists")
        void shouldReturnCollectionWhenExists() {
            Collection result = service.getById(1L);

            assertNotNull(result);
            assertEquals(1L, result.id());
            assertEquals("Thriller", result.name());
        }

        @Test
        @DisplayName("should throw exception when ID does not exist")
        void shouldThrowWhenIdNotExists() {
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(999L)
            );

            assertTrue(exception.getMessage().contains("Collection with ID 999 does not exist"));
        }
    }
}

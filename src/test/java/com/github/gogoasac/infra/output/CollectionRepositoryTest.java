package com.github.gogoasac.infra.output;

import com.github.gogoasac.domain.entity.Collection;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CollectionRepository Tests")
class CollectionRepositoryTest {
    private static final String FILE_PATH = "Collections.json";
    private CollectionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CollectionRepository();
    }

    @AfterEach
    void tearDown() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }

    @Nested
    @DisplayName("save method tests")
    class SaveTests {
        @Test
        @DisplayName("Should save new collection with generated ID")
        void save_ShouldCreateNewCollectionWithGeneratedId() {
            Collection collection = new Collection(null, "Test Collection");

            Collection savedCollection = repository.addCollection(collection);

            assertNotNull(savedCollection.id());
            assertEquals(collection.name(), savedCollection.name());
        }

        @Test
        @DisplayName("Should generate unique IDs for multiple collections")
        void save_ShouldGenerateUniqueIds() {
            Collection collection1 = new Collection(null, "Collection 1");
            Collection collection2 = new Collection(null, "Collection 2");

            Collection savedCollection1 = repository.addCollection(collection1);
            Collection savedCollection2 = repository.addCollection(collection2);

            assertNotEquals(savedCollection1.id(), savedCollection2.id());
        }

        @Test
        @DisplayName("Should create distinct entries for collections with same name")
        void save_WithSameName_ShouldCreateDistinctEntries() {
            String collectionName = "Duplicate Name";

            Collection collection1 = repository.addCollection(new Collection(null, collectionName));
            Collection collection2 = repository.addCollection(new Collection(null, collectionName));

            assertNotEquals(collection1.id(), collection2.id());
            List<Collection> collections = repository.findAll();
            assertEquals(2, collections.stream()
                .filter(c -> c.name().equals(collectionName))
                .count());
        }
    }

    @Nested
    @DisplayName("findById method tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return collection when exists")
        void findById_WhenCollectionExists_ShouldReturnCollection() {
            Collection collection = repository.addCollection(new Collection(null, "Test Collection"));

            Optional<Collection> found = repository.findById(collection.id());

            assertTrue(found.isPresent());
            assertEquals(collection.name(), found.get().name());
        }

        @Test
        @DisplayName("Should return empty when collection doesn't exist")
        void findById_WhenCollectionDoesNotExist_ShouldReturnEmpty() {
            Optional<Collection> found = repository.findById(999L);

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("findAll method tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all collections when collections exist")
        void findAll_WhenCollectionsExist_ShouldReturnAllCollections() {
            Collection collection1 = repository.addCollection(new Collection(null, "Collection 1"));
            Collection collection2 = repository.addCollection(new Collection(null, "Collection 2"));

            List<Collection> collections = repository.findAll();

            assertEquals(2, collections.size());
            assertTrue(collections.stream().anyMatch(c -> c.name().equals("Collection 1")));
            assertTrue(collections.stream().anyMatch(c -> c.name().equals("Collection 2")));
        }

        @Test
        @DisplayName("Should return empty list when no collections exist")
        void findAll_WhenNoCollections_ShouldReturnEmptyList() {
            List<Collection> collections = repository.findAll();

            assertTrue(collections.isEmpty());
        }
    }

    @Nested
    @DisplayName("Persistence behavior tests")
    class PersistenceTests {
        @Test
        @DisplayName("Should persist data between repository instances")
        void persistenceTest_ShouldPersistBetweenRepositoryInstances() {
            Collection collection = repository.addCollection(new Collection(null, "Persistent Collection"));

            CollectionRepository newRepository = new CollectionRepository();
            Optional<Collection> found = newRepository.findById(collection.id());

            assertTrue(found.isPresent());
            assertEquals(collection.name(), found.get().name());
        }

        @Test
        @DisplayName("Should handle file creation correctly")
        void fileOperations_ShouldCreateFileIfNotExists() {
            File file = new File(FILE_PATH);
            file.delete();

            new CollectionRepository();

            assertTrue(file.exists());
        }
    }
}

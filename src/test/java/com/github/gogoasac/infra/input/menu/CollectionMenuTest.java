package com.github.gogoasac.infra.input.menu;

import com.github.gogoasac.application.dto.AddCollectionCommand;
import com.github.gogoasac.application.input.CollectionManagementInput;
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

@DisplayName("CollectionMenu Tests")
class CollectionMenuTest {

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
        void shouldAddCollectionAndThenListIt() {
            // Input sequence: 1 (Add), "My Collection" (name), 2 (List), 9 (Back)
            final String inputLines = String.join("\n", "1", "My Collection", "2", "9") + "\n";
            final BufferedReader sharedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));

            final MutableCollectionInput collectionInput = new MutableCollectionInput();
            final CollectionMenu menu = new CollectionMenu(printStream, sharedReader, collectionInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Collection created"), "Should report creation");
            assertTrue(output.contains("My Collection"), "Should include collection name in output");
            assertTrue(output.contains("Collections:"), "Should list collections");

            final List<Collection> persisted = collectionInput.getAll();
            assertEquals(1, persisted.size(), "One collection should be persisted");
            assertEquals("My Collection", persisted.getFirst().name());
        }
    }

    @Nested
    @DisplayName("View by id flows")
    class ViewById {

        @Test
        void shouldViewCollectionByIdWhenExists() {
            final MutableCollectionInput collectionInput = new MutableCollectionInput();
            final Collection created = collectionInput.addCollection(new AddCollectionCommand("Existing Coll"));

            final String inputLines = String.join("\n", "3", String.valueOf(created.id()), "9") + "\n";
            final BufferedReader sharedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            final CollectionMenu menu = new CollectionMenu(printStream, sharedReader, collectionInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Collection:"), "Should print collection header");
            assertTrue(output.contains("Existing Coll"), "Should include the collection's name");
        }

        @Test
        void shouldReportInvalidNumberWhenNonNumericIdProvided() {
            final String inputLines = String.join("\n", "3", "abc", "9") + "\n";
            final BufferedReader sharedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            final MutableCollectionInput collectionInput = new MutableCollectionInput();
            final CollectionMenu menu = new CollectionMenu(printStream, sharedReader, collectionInput);

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
        void shouldRejectEmptyNameWhenAddingCollection() {
            final String inputLines = String.join("\n", "1", "", "9") + "\n";
            final BufferedReader sharedReader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(inputLines.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            final MutableCollectionInput collectionInput = new MutableCollectionInput();
            final CollectionMenu menu = new CollectionMenu(printStream, sharedReader, collectionInput);

            menu.run();

            final String output = outputBuffer.toString(StandardCharsets.UTF_8);
            assertTrue(output.contains("Name cannot be empty"), "Should reject empty collection name");
            assertEquals(0, collectionInput.getAll().size(), "No collection should be persisted");
        }
    }

    // --- lightweight in-memory mock for CollectionManagementInput used by tests ---
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
}